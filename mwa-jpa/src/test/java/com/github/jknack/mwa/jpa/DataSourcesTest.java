package com.github.jknack.mwa.jpa;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Driver;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * Unit test for {@link DataSources}.
 *
 * @author edgar.espina
 * @since 0.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DataSources.class, EmbeddedDatabaseBuilder.class })
public class DataSourcesTest {

  @Test
  public void mem() throws Exception {
    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty("db")).andReturn("mem");

    EmbeddedDatabase datasource = createMock(EmbeddedDatabase.class);

    EmbeddedDatabaseBuilder embeddedDatabaseBuilder =
        PowerMock.createMockAndExpectNew(EmbeddedDatabaseBuilder.class);
    expect(embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2)).andReturn(
        embeddedDatabaseBuilder);
    expect(embeddedDatabaseBuilder.build()).andReturn(datasource);

    PowerMock.replay(EmbeddedDatabaseBuilder.class);
    replay(env, embeddedDatabaseBuilder);

    DataSource result = DataSources.build(env);
    assertEquals(datasource, result);

    verify(env, embeddedDatabaseBuilder);
    PowerMock.verify(EmbeddedDatabaseBuilder.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void fs() throws Exception {
    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty("db")).andReturn("fs");
    expect(env.getProperty("application.name", "testdb")).andReturn("fsdb");

    SimpleDriverDataSource dataSource =
        PowerMock.createMockAndExpectNew(SimpleDriverDataSource.class);
    dataSource.setDriverClass((Class<? extends Driver>) Class
        .forName("org.h2.Driver"));
    expectLastCall();
    String databaseUrl =
        String.format("jdbc:h2:%s" + File.separator + "fsdb",
            System.getProperty("java.io.tmpdir"));
    dataSource.setUrl(databaseUrl);
    expectLastCall();
    dataSource.setUsername("sa");
    expectLastCall();
    dataSource.setPassword("");
    expectLastCall();

    PowerMock.replay(SimpleDriverDataSource.class);
    replay(env, dataSource);

    DataSource result = DataSources.build(env);
    assertEquals(dataSource, result);

    verify(env, dataSource);
    PowerMock.verify(SimpleDriverDataSource.class);
  }

  @Test
  public void highPerformanceDS() throws Exception {
    String database = "jdbc:real:db";
    String dbDriver = "com.my.Driver";
    String dbUser = "user";
    String dbPass = "pass";

    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty("db")).andReturn(database);
    expect(env.getProperty(DataSources.DB_DRIVER, "com.mysql.jdbc.Driver"))
        .andReturn(dbDriver);
    expect(env.getRequiredProperty(DataSources.DB_USER))
        .andReturn(dbUser);
    expect(env.getRequiredProperty(DataSources.DB_PASSWORD))
        .andReturn(dbPass);
    expect(
        env.getProperty(DataSources.DB_IDDLE_CONNECTION_TEST_PERIOD,
            Integer.class, 14400))
        .andReturn(14400);
    expect(env.getProperty(DataSources.DB_IDDLE_MAX_AGE, Integer.class, 3600))
        .andReturn(3600);
    expect(
        env.getProperty(DataSources.DB_MAX_CONNECTIONS_PER_PARTITION,
            Integer.class, 30))
        .andReturn(30);
    expect(
        env.getProperty(DataSources.DB_MIN_CONNECTIONS_PER_PARTITION,
            Integer.class, 10))
        .andReturn(10);
    expect(
        env.getProperty(DataSources.DB_PARTITION_COUNT,
            Integer.class, 3))
        .andReturn(3);
    expect(
        env.getProperty(DataSources.DB_ACQUIRE_INCREMENT,
            Integer.class, 5))
        .andReturn(5);
    expect(
        env.getProperty(DataSources.DB_STATEMENTS_CACHE_SIZE,
            Integer.class, 20))
        .andReturn(20);
    expect(
        env.getProperty(DataSources.DB_RELEASE_THREADS,
            Integer.class, 3))
        .andReturn(3);

    BoneCPDataSource dataSource =
        PowerMock.createMockAndExpectNew(BoneCPDataSource.class);
    dataSource.setJdbcUrl(database);
    expectLastCall();
    dataSource.setDriverClass(dbDriver);
    expectLastCall();
    dataSource.setUsername(dbUser);
    expectLastCall();
    dataSource.setPassword(dbPass);
    expectLastCall();
    dataSource.setIdleConnectionTestPeriod(14400, TimeUnit.SECONDS);
    expectLastCall();
    dataSource.setIdleMaxAge(3600, TimeUnit.SECONDS);
    expectLastCall();
    dataSource.setMaxConnectionsPerPartition(30);
    expectLastCall();
    dataSource.setMinConnectionsPerPartition(10);
    expectLastCall();
    dataSource.setPartitionCount(3);
    expectLastCall();
    dataSource.setAcquireIncrement(5);
    expectLastCall();
    dataSource.setStatementsCacheSize(20);
    expectLastCall();
    dataSource.setReleaseHelperThreads(3);
    expectLastCall();

    PowerMock.replay(BoneCPDataSource.class);
    replay(env, dataSource);

    DataSource result = DataSources.build(env);
    assertEquals(dataSource, result);

    verify(env, dataSource);
    PowerMock.verify(BoneCPDataSource.class);
  }
}
