package com.github.edgarespina.mwa.jpa;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.github.edgarespina.mwa.handler.MessageConverterHandlerExceptionResolver;

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
   * @param configurers The list of JPA configurer. Required.
   * @return A {@link EntityManagerFactory object} available for use. Spring
   *         managed beans can use the {@link EntityManager service} using the
   *         {@link PersistenceContext annotation}.
   * @throws ClassNotFoundException If the driver class cannot be loaded.
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      final Environment env, final JpaConfigurer[] configurers)
      throws ClassNotFoundException {
    logger.info("Starting service: {}",
        EntityManagerFactory.class.getSimpleName());
    LocalContainerEntityManagerFactoryBean emf =
        new LocalContainerEntityManagerFactoryBean();
    String hbm2ddl = env.getProperty(DB_SCHEMA, "update");
    Map<String, String> properties = new HashMap<String, String>();
    logger.info("  schema's mode: {}", hbm2ddl);
    properties.put(AvailableSettings.HBM2DDL_AUTO, hbm2ddl);
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setJpaPropertyMap(properties);
    emf.setDataSource(dataSource(env));
    emf.setPackagesToScan("__DONT_SCAN_");
    emf.setPersistenceUnitPostProcessors(new PersistenceUnitPostProcessor() {
      @Override
      public void postProcessPersistenceUnitInfo(
          final MutablePersistenceUnitInfo pui) {
        Set<String> entityNames = new HashSet<String>();
        for (JpaConfigurer configurer : configurers) {
          Set<Class<?>> entities = configurer.scan();
          for (Class<?> entity : entities) {
            if (entityNames.add(entity.getName())) {
              pui.addManagedClassName(entity.getName());
            }
          }
        }
      }
    });
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
   * Publish a {@link DataAccessHandlerExceptionResolver} message resolver.
   *
   * @return A new {@link DataAccessHandlerExceptionResolver} message resolver.
   */
  @Bean
  public HandlerExceptionResolver dataAccessExceptionResolver() {
    return new DataAccessHandlerExceptionResolver();
  }

  /**
   * Publish a {@link HandlerExceptionResolver} message converter resolver for
   * {@link PersistenceException}.
   *
   * @return A new {@link HandlerExceptionResolver} message converter resolver
   *         for {@link PersistenceException}.
   */
  @Bean
  public HandlerExceptionResolver persistenceExceptionResolver() {
    return new MessageConverterHandlerExceptionResolver(
        PersistenceException.class);
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
