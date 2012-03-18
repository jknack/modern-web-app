package org.knowhow.mwa.jpa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.sql.Driver;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.util.ClassUtils;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * <p>
 * A {@link DataSource} builder for a modern web application architecture. It
 * depends on {@link Environment Spring's environment class} for reading
 * database configuration and offers the following functionality:
 * </p>
 * <ul>
 * <li>An embedded database if the {@link #DATABASE db} property is one of:
 * 'mem' or 'fs'. Useful for during development.
 * <li>A high performance connection pool if the {@link #DATABASE db} property
 * isn't one of: h2, derby, or hsql. See: BoneCP.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 * @see Environment
 * @see EmbeddedDatabaseBuilder
 * @see <a href="http://www.http://jolbox.com/">BoneCP</a>
 */
public final class DataSources {

  /**
   * The H2 driver class's name.
   */
  private static final String H2_DRIVER = "org.h2.Driver";

  /**
   * The database property. One of h2, derby or hsql for in-memory database or a
   * full database's url for a high performance connection pool. Required.
   */
  public static final String DATABASE = "db";

  /**
   * The database's user name. Required for a high performance connection pool.
   */
  public static final String DB_USER = "db.user";

  /**
   * The database's password. Required for a high performance connection pool.
   */
  public static final String DB_PASSWORD = "db.password";

  /**
   * The database's driver name. Required for a high performance connection
   * pool. Default is: {@link #DB_DEFAULT_DRIVER}.
   */
  public static final String DB_DRIVER = "db.driver";

  /**
   * The default database's driver for {@link #DB_DRIVER}.
   */
  public static final String DB_DEFAULT_DRIVER = "com.mysql.jdbc.Driver";

  /**
   * Time for a connection to remain idle before sending a test query to the DB.
   * Advanced usage.
   */
  public static final String DB_IDDLE_CONNECTION_TEST_PERIOD =
      "db.iddleConnectionTestPeriod";

  /**
   * Time for a connection in seconds to remain idle before sending a test query
   * to the DB. Advanced usage. See {@link #DB_IDDLE_CONNECTION_TEST_PERIOD}.
   */
  public static final int DB_DEFAULT_IDDLE_CONNECTION_TEST_PERIOD = 14400;

  /**
   * The time, for a connection to remain unused before it is closed off.
   */
  public static final String DB_IDDLE_MAX_AGE = "db.iddleMaxAge";

  /**
   * The time, for a connection to remain unused before it is closed off.
   */
  public static final int DB_DEFAULT_IDDLE_MAX_AGE = 3600;

  /**
   * The maximum number of connections that will be contained in every
   * partition. Setting this to 5 with 3 partitions means you will have 15
   * unique connections to the database. Note that the connection pool will not
   * create all these connections in one go but rather start off with
   * minConnectionsPerPartition and gradually increase connections as required.
   */
  public static final String DB_MAX_CONNECTIONS_PER_PARTITION =
      "db.maxConnectionsPerPartition";

  /**
   * The default value for {@link #DB_MAX_CONNECTIONS_PER_PARTITION}.
   */
  public static final int DB_DEFAULT_MAX_CONNECTIONS_PER_PARTITION = 30;

  /**
   * The minimum number of connections that will be contained in every
   * partition.
   */
  public static final String DB_MIN_CONNECTIONS_PER_PARTITION =
      "db.minConnectionsPerPartition";

  /**
   * The default value for {@link #DB_MIN_CONNECTIONS_PER_PARTITION}.
   */
  public static final int DB_DEFAULT_MIN_CONNECTIONS_PER_PARTITION = 10;

  /**
   * In order to reduce lock contention and thus improve performance, each
   * incoming connection request picks off a connection from a pool that has
   * thread-affinity, i.e. pool[threadId % partition_count]. The higher this
   * number, the better your performance will be for the case when you have
   * plenty of short-lived threads. Beyond a certain threshold, maintenance of
   * these pools will start to have a negative effect on performance (and only
   * for the case when connections on a partition start running out).
   */
  public static final String DB_PARTITION_COUNT = "db.partitionCount";

  /**
   * Default value for {@link #DB_PARTITION_COUNT}.
   */
  public static final int DB_DEFAULT_PARTITION_COUNT = 3;

  /**
   * When the available connections are about to run out, BoneCP will
   * dynamically create new ones in batches. This property controls how many new
   * connections to create in one go (up to a maximum of
   * maxConnectionsPerPartition).
   * <p>
   * Note: This is a per partition setting.
   * </p>
   */
  public static final String DB_ACQUIRE_INCREMENT = "db.acquireIncrement";

  /**
   * Default value for {@link #DB_ACQUIRE_INCREMENT}.
   */
  public static final int DB_DEFAULT_ACQUIRE_INCREMENT = 5;

  /**
   * The number of statements to cache.
   */
  public static final String DB_STATEMENTS_CACHE_SIZE =
      "db.statementsCacheSize";

  /**
   * Default value for {@link #DB_STATEMENTS_CACHE_SIZE}.
   */
  public static final int DB_DEFAULT_STATEMENTS_CACHE_SIZE = 20;

  /**
   * <p>
   * Sets number of helper threads to create that will handle releasing a
   * connection.
   * </p>
   * <p>
   * When this value is set to zero, the application thread is blocked until the
   * pool is able to perform all the necessary cleanup torecycle the connection
   * and make it available for another thread.
   * </p>
   * <p>
   * When a non-zero value is set, the pool will create threads that will take
   * care of recycling a connection when it is closed (the application dumps the
   * connection into a temporary queue to be processed asychronously to the
   * application via the release helper threads).
   * </p>
   * <p>
   * Useful when your application is doing lots of work on each connection (i.e.
   * perform an SQL query, do lots of non-DB stuff and perform another query),
   * otherwise will probably slow things down.
   * </p>
   */
  public static final String DB_RELEASE_THREADS = "db.releaseThreads";

  /**
   * Default value for {@link #DB_RELEASE_THREADS}.
   */
  public static final int DB_DEFAULT_RELEASE_THREADS = 3;

  /**
   * The logging system.
   */
  private static Logger logger = LoggerFactory
      .getLogger(DataSources.class);

  /**
   * Not allowed.
   */
  private DataSources() {
  }

  /**
   * <ul>
   * <li>An embedded or in-memory database if the {@link #DATABASE db} property
   * is one of: mem or file. Useful for during development.
   * <li>A high performance connection pool if the {@link #DATABASE db} property
   * isn't one of: h2, derby, or hsql. See: BoneCP.
   * </ul>
   *
   * @param environment The application's environment. Required.
   * @return A new {@link DataSource} object.
   * @throws ClassNotFoundException If the driver isn't found.
   */
  public static DataSource build(final Environment environment)
      throws ClassNotFoundException {
    checkNotNull(environment, "The environment is required.");
    String database = environment.getRequiredProperty(DATABASE);
    DataSource dataSource = createEmbeddedDatabase(database);
    if (dataSource == null) {
      dataSource = newPooledDataSource(environment, database);
    }
    return dataSource;
  }

  /**
   * Build a high performance connection pool datasource.
   *
   * @param env The environment.
   * @param database The database type.
   * @return A high performance connection pool datasource.
   */
  private static DataSource newPooledDataSource(final Environment env,
      final String database) {
    BoneCPDataSource datasource = new BoneCPDataSource();
    datasource.setJdbcUrl(database);
    datasource.setDriverClass(env.getProperty(DB_DRIVER,
        DB_DEFAULT_DRIVER));
    datasource.setUsername(env.getRequiredProperty(DB_USER));
    datasource.setPassword(env.getRequiredProperty(DB_PASSWORD));
    datasource.setIdleConnectionTestPeriod(env.getProperty(
        DB_IDDLE_CONNECTION_TEST_PERIOD, Integer.class,
        DB_DEFAULT_IDDLE_CONNECTION_TEST_PERIOD), TimeUnit.SECONDS);
    datasource.setIdleMaxAge(env.getProperty(DB_IDDLE_MAX_AGE,
        Integer.class, DB_DEFAULT_IDDLE_MAX_AGE), TimeUnit.SECONDS);
    datasource.setMaxConnectionsPerPartition(env.getProperty(
        DB_MAX_CONNECTIONS_PER_PARTITION, Integer.class,
        DB_DEFAULT_MAX_CONNECTIONS_PER_PARTITION));
    datasource.setMinConnectionsPerPartition(env.getProperty(
        DB_MIN_CONNECTIONS_PER_PARTITION, Integer.class,
        DB_DEFAULT_MIN_CONNECTIONS_PER_PARTITION));
    datasource.setPartitionCount(env.getProperty(DB_PARTITION_COUNT,
        Integer.class, DB_DEFAULT_PARTITION_COUNT));
    datasource.setAcquireIncrement(env.getProperty(DB_ACQUIRE_INCREMENT,
        Integer.class, DB_DEFAULT_ACQUIRE_INCREMENT));
    datasource.setStatementsCacheSize(env.getProperty(
        DB_STATEMENTS_CACHE_SIZE, Integer.class,
        DB_DEFAULT_STATEMENTS_CACHE_SIZE));
    datasource.setReleaseHelperThreads(env.getProperty(DB_RELEASE_THREADS,
        Integer.class, DB_DEFAULT_RELEASE_THREADS));
    logger.info("Creating high performance connection pool: '{}'", database);
    return datasource;
  }

  /**
   * Create an embedded database.
   *
   * @param database The database type.
   * @return A new embedded database or null.
   * @throws ClassNotFoundException If the driver isn't found.
   */
  private static DataSource createEmbeddedDatabase(
      final String database) throws ClassNotFoundException {
    if ("mem".equalsIgnoreCase(database)) {
      return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
          .build();
    } else if ("fs".equalsIgnoreCase(database)) {
      String tmpdir = System.getProperty("java.io.tmpdir");
      String jdbcUrl = "jdbc:h2:" + tmpdir + File.separator + "testdb";
      SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
      @SuppressWarnings("unchecked")
      Class<? extends Driver> driverClass =
          (Class<? extends Driver>) ClassUtils.forName(H2_DRIVER,
              DataSources.class.getClassLoader());
      dataSource.setDriverClass(driverClass);
      dataSource.setUrl(jdbcUrl);
      dataSource.setUsername("sa");
      dataSource.setPassword("");
      logger.info("Creating embedded database: '{}'", jdbcUrl);
      return dataSource;
    } else {
      return null;
    }
  }

}
