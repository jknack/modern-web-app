package com.github.jknack.mwa.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgresPlusDialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.Sybase11Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * <p>
 * A JPA Spring Module. It offers the following functionality:
 * </p>
 * <ul>
 * <li>A {@link DataSource} ready for development or production. See {@link DataSources}.
 * <li>An {@link EntityManager} and {@link EntityManagerFactory} ready for dependency injection.
 * <li>A {@link JpaTransactionManager platform transaction manager}.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class JpaModule {

  /**
   * Default dialect.
   *
   * @author edgar.espina
   *
   */
  private enum DefaultDialect {
    /**
     * Db2 db.
     */
    DB2 {
      @Override
      public Class<?> dialect() {
        return DB2Dialect.class;
      }
    },

    /**
     * Derby db.
     */
    DERBY {
      @Override
      public Class<?> dialect() {
        return DerbyTenFiveDialect.class;
      }
    },

    /**
     * H2 db.
     */
    H2 {
      @Override
      public Class<?> dialect() {
        return H2Dialect.class;
      }

      @Override
      public boolean apply(final String db) {
        return "mem".equals(db) || "fs".equals(db) || super.apply(db);
      }
    },

    /**
     * HSQL db.
     */
    HSQLDB {
      @Override
      public Class<?> dialect() {
        return HSQLDialect.class;
      }
    },

    /**
     * Informix db.
     */
    INFORMIX {
      @Override
      public Class<?> dialect() {
        return InformixDialect.class;
      }
    },

    /**
     * mySQL db.
     */
    MySQL {
      @Override
      public Class<?> dialect() {
        return MySQL5InnoDBDialect.class;
      }
    },

    /**
     * Oracle db.
     */
    ORACLE {
      @Override
      public Class<?> dialect() {
        return Oracle10gDialect.class;
      }
    },

    /**
     * Postgres db.
     */
    POSTGRESQL {
      @Override
      public Class<?> dialect() {
        return PostgresPlusDialect.class;
      }
    },

    /**
     * SQL Server.
     */
    SQL_SERVER {
      @Override
      public Class<?> dialect() {
        return SQLServer2008Dialect.class;
      }
    },

    /**
     * Sybase db.
     */
    SYBASE {
      @Override
      public Class<?> dialect() {
        return Sybase11Dialect.class;
      }
    };

    /**
     * True if the given database matches the dialect.
     *
     * @param db The database uri.
     * @return True if the given database matches the dialect.
     */
    public boolean apply(final String db) {
      String prefix = "jdbc:" + name().toLowerCase();
      return db.startsWith(prefix);
    }

    /**
     * The dialect's class.
     *
     * @return The dialect's class.
     */
    public abstract Class<?> dialect();

    /**
     * Find a default dialect for the given database.
     *
     * @param db The database uri.
     * @return A default dialect or <code>null</code>.
     */
    public static Class<?> dialect(final String db) {
      DefaultDialect[] dialects = values();
      for (DefaultDialect dialect : dialects) {
        if (dialect.apply(db)) {
          return dialect.dialect();
        }
      }
      return null;
    }
  }

  /**
   * The database's schema mode: create, create-drop, update, validate.
   */
  public static final String DB_SCHEMA = "db.schema";

  /**
   * The logging system.
   */
  private static Logger logger = LoggerFactory.getLogger(JpaModule.class);

  /**
   * <ul>
   * <li>An embedded or in-memory database if the {@link #DATABASE db} property is one of: h2, derby
   * or hsql. Useful for during development.
   * <li>A high performance connection pool if the {@link #DATABASE db} property isn't one of: h2,
   * derby, or hsql. See: BoneCP.
   * </ul>
   *
   * @param env The application environment. Required.
   * @return A new {@link DataSource}.
   * @throws ClassNotFoundException If the driver class cannot be loaded.
   * @see DataSources
   */
  @Bean
  public DataSource jpaDataSource(final Environment env) throws ClassNotFoundException {
    return DataSources.build(env);
  }

  /**
   * Create a {@link JpaTransactionManager service}. This service is
   * responsibly for providing JDBC transactions to classes/methods annotated
   * with {@link Transactional annotation}.
   *
   * @param emf The {@link EntityManagerFactory} resource. Required.
   * @return A {@link JpaTransactionManager service}. This service is responsibly for providing JDBC
   *         transactions to classes/methods annotated with {@link Transactional annotation}.
   */
  @Bean(name = {"jpaTransactionManager", "transactionManager" })
  public JpaTransactionManager jpaTransactionManager(final EntityManagerFactory emf) {
    logger.info("Starting service: {}", JpaTransactionManager.class.getSimpleName());
    return new JpaTransactionManager(emf);
  }

  /**
   * Produce a {@link EntityManagerFactory object}. Spring beans can use the {@link EntityManager
   * service} using the {@link PersistenceContext
   * annotation}.
   *
   * @param env The application environment. Required.
   * @param namespace The package to be scanned. Required.
   * @return A {@link EntityManagerFactory object} available for use. Spring managed beans can use
   *         the {@link EntityManager service} using the {@link PersistenceContext annotation}.
   * @throws ClassNotFoundException If the driver class cannot be loaded.
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean jpaEntityManagerFactory(final Environment env,
      @Named("application.ns") final String[] namespace) throws ClassNotFoundException {
    logger.info("Starting service: {}", EntityManagerFactory.class.getSimpleName());
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    String hbm2ddl = env.getProperty(DB_SCHEMA, "update");
    final Map<String, String> properties = new HashMap<String, String>();
    logger.info("  schema's mode: {}", hbm2ddl);
    properties.put(AvailableSettings.HBM2DDL_AUTO, hbm2ddl);

    // default dialect
    Class<?> dialect = DefaultDialect.dialect(env.getRequiredProperty(DataSources.DATABASE));
    if (dialect != null) {
      properties.put(AvailableSettings.DIALECT, dialect.getName());
    }

    /**
     * Looks for Hibernate properties and set them all.
     */
    ReflectionUtils.doWithFields(AvailableSettings.class, new FieldCallback() {
      @Override
      public void doWith(final Field field) throws IllegalAccessException {
        String propertyName = (String) field.get(null);
        String propertyValue = env.getProperty(propertyName);
        if (!StringUtils.isEmpty(propertyValue)) {
          properties.put(propertyName, propertyValue);
        }
      }
    }, new FieldFilter() {
      @Override
      public boolean matches(final Field field) {
        int mods = field.getModifiers();
        return field.getType() == String.class && Modifier.isStatic(mods) && Modifier.isFinal(mods);
      }
    });

    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setJpaPropertyMap(properties);
    emf.setDataSource(jpaDataSource(env));
    emf.setPackagesToScan(namespace);
    return emf;
  }

  /**
   * Enable injection of {@link EntityManager} using {@link Inject}. Useful for
   * constructor injection.
   *
   * @return A shared entity manager.
   */
  @Bean
  public SharedEntityManagerBean jpaEntityManager() {
    logger.info("Starting service: {}", EntityManager.class.getSimpleName());
    return new SharedEntityManagerBean();
  }

  /**
   * Perform cleanup tasks.
   *
   * @throws Exception If something goes wrong during shutdown.
   */
  @PreDestroy
  public void destroy() throws Exception {
    cleanupDrivers();
  }

  /**
   * De-register jdbc's drivers and help Tomcat 6.x to not complain about memory
   * leaks.
   *
   * @throws SQLException If the driver cannot be de-allocated.
   */
  private void cleanupDrivers() throws SQLException {
    Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      logger.debug("De-registering JDBC's driver: {}", driver.getClass()
          .getName());
      DriverManager.deregisterDriver(driver);
    }
  }

}
