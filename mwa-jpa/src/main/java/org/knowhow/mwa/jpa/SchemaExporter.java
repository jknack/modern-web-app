package org.knowhow.mwa.jpa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Type;
import org.hibernate.tool.hbm2ddl.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * A database schema exporter for hibernate.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class SchemaExporter {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(SchemaExporter.class);

  /**
   * Alias for Hibernate dialects.
   */
  @SuppressWarnings("serial")
  private static final Map<Database, String> dialects =
      new HashMap<Database, String>() {
        {
          Database[] databases = Database.values();
          for (Database database : databases) {
            AbstractJpaVendorAdapter vendorAdapter =
                new HibernateJpaVendorAdapter();
            vendorAdapter.setDatabase(database);
            put(database,
                (String) vendorAdapter.getJpaPropertyMap().get(
                    Environment.DIALECT));
          }
          // Override mySQL with InnoDB5
          put(Database.MYSQL, MySQL5InnoDBDialect.class.getName());
        }
      };

  /**
   * The script statement delimiter.
   */
  private String delimiter = ";";

  /**
   * Set the script statement delimiter. Default is: ';'.
   *
   * @param delimiter The script statement delimiter. Required.
   * @return This {@link SchemaExporter}.
   */
  public SchemaExporter withDelimiter(final String delimiter) {
    this.delimiter =
        checkNotNull(delimiter, "The statement delimiter is required");
    return this;
  }

  /**
   * Export the schema database to a file using a {@link JpaConfigurer}.
   *
   * @param database The database's type. Required.
   * @param output The output file. Required.
   * @param packagesToScan Where the persistent class are. Required.
   * @throws Exception If the schema cannot be exported.
   */
  public void export(final Database database, final File output,
      final String... packagesToScan) throws Exception {
    JpaConfigurer configurer = new JpaConfigurer();
    configurer.addPackages(packagesToScan);
    export(database, output, configurer);
  }

  /**
   * Export the schema database to a file using a {@link JpaConfigurer}.
   *
   * @param database The database's type. Required.
   * @param output The output file. Required.
   * @param configurer The persistent classes. Required.
   */
  public void export(final Database database, final File output,
      final JpaConfigurer configurer) {
    checkNotNull(database, "The database is required.");
    checkNotNull(output, "The output file is required.");
    checkNotNull(configurer, "The entity configurer is required.");
    String dialect = dialects.get(database);
    logger.info("Generating: {}", output);
    logger.info("  database: {}, dialect: {}", database, dialect);

    Configuration configuration = new Configuration();
    // Add the persistent classes here.
    for (Class<?> persistenceClass : configurer.scan()) {
      logger.info("Adding class: {}", persistenceClass.getName());
      configuration.addAnnotatedClass(persistenceClass);
    }
    configuration.setProperty(Environment.DIALECT, dialect);

    new SchemaExport(configuration)
        .setDelimiter(delimiter)
        .setOutputFile(output.getAbsolutePath())
        .execute(Target.SCRIPT, Type.BOTH);
    logger.info("DDL: {}", output);
  }

}
