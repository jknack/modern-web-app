package com.github.jknack.mwa.jpa;

import java.lang.reflect.Field;
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

import org.hibernate.cfg.AvailableSettings;
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

/**
 * <p>
 * A JPA Spring Module. It offers the following functionality:
 * </p>
 * <ul>
 * <li>A {@link DataSource} ready for development or production. See
 * {@link DataSources}.
 * <li>An {@link EntityManager} and {@link EntityManagerFactory} ready for
 * dependency injection.
 * <li>A {@link JpaTransactionManager platform transaction manager}.
 * </ul>
 * <p>
 * Dependencies:
 * </p>
 * <ul>
 * <li>The Spring Application Context must provide a {@link JpaConfigurer} bean.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
@EnableTransactionManagement
public class JpaModule {

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
   * <li>An embedded or in-memory database if the {@link #DATABASE db} property
   * is one of: h2, derby or hsql. Useful for during development.
   * <li>A high performance connection pool if the {@link #DATABASE db} property
   * isn't one of: h2, derby, or hsql. See: BoneCP.
   * </ul>
   *
   * @param env The application environment. Required.
   * @return A new {@link DataSource}.
   * @throws ClassNotFoundException If the driver class cannot be loaded.
   * @see DataSources
   */
  @Bean
  public DataSource dataSource(final Environment env)
      throws ClassNotFoundException {
    return DataSources.build(env);
  }

  /**
   * Create a {@link JpaTransactionManager service}. This service is
   * responsibly for providing JDBC transactions to classes/methods annotated
   * with {@link Transactional annotation}.
   *
   * @param emf The {@link EntityManagerFactory} resource. Required.
   * @return A {@link JpaTransactionManager service}. This service is
   *         responsibly for providing JDBC transactions to classes/methods
   *         annotated with {@link Transactional annotation}.
   */
  @Bean
  public JpaTransactionManager
      transactionManager(final EntityManagerFactory emf) {
    logger.info("Starting service: {}",
        JpaTransactionManager.class.getSimpleName());
    return new JpaTransactionManager(emf);
  }

  /**
   * Produce a {@link EntityManagerFactory object}. Spring beans can use the
   * {@link EntityManager service} using the {@link PersistenceContext
   * annotation}.
   *
   * @param env The application environment. Required.
   * @param namespace The package to be scanned. Required.
   * @return A {@link EntityManagerFactory object} available for use. Spring
   *         managed beans can use the {@link EntityManager service} using the
   *         {@link PersistenceContext annotation}.
   * @throws ClassNotFoundException If the driver class cannot be loaded.
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      final Environment env, @Named("application.ns") final String[] namespace)
      throws ClassNotFoundException {
    logger.info("Starting service: {}",
        EntityManagerFactory.class.getSimpleName());
    LocalContainerEntityManagerFactoryBean emf =
        new LocalContainerEntityManagerFactoryBean();
    String hbm2ddl = env.getProperty(DB_SCHEMA, "update");
    final Map<String, String> properties = new HashMap<String, String>();
    logger.info("  schema's mode: {}", hbm2ddl);
    properties.put(AvailableSettings.HBM2DDL_AUTO, hbm2ddl);

    ReflectionUtils.doWithFields(AvailableSettings.class, new FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException,
          IllegalAccessException {
        String propertyName = (String) field.get(null);
        if (env.getProperty(propertyName) != null) {
          properties.put(propertyName, env.getProperty(propertyName));
        }
      }
    });

    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setJpaPropertyMap(properties);
    emf.setDataSource(dataSource(env));
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
  public SharedEntityManagerBean entityManager() {
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
