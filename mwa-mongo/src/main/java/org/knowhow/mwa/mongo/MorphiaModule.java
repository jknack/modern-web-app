package org.knowhow.mwa.mongo;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * <p>
 * Add support for {@link Morphia} on a Mongo database. Clients must provide a
 * instance of {@link MorphiaConfigurer} to be able to detect Morphia persistent
 * classes.
 * </p>
 * <p>
 * Also, it configure a Morphia {@link Datastore} ready to use.
 * </p>
 *
 * @author edgar.espina
 * @see MongoModule
 * @see MorphiaConfigurer
 */
@Configuration
@Import(MongoModule.class)
public class MorphiaModule {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(MorphiaModule.class);

  /**
   * Publish a {@link Morphia} POJOs mapper for Mongo datatabases.
   *
   * @param configurer The persistent class provider. Required.
   * @return A {@link Morphia} POJOs mapper for Mongo datatabases.
   */
  @Bean
  public Morphia morphia(final MorphiaConfigurer configurer) {
    Validate.notNull(configurer, "The morphia configurer is required.");
    Morphia morphia = new Morphia();
    for (Class<?> document : configurer.getClasses()) {
      logger.debug("Adding morphia class: {}", document.getName());
      morphia.map(document);
    }
    return morphia;
  }

  /**
   * Publish a Morphia {@link Datastore} for executing CRUD operations over
   * POJOs.
   *
   * @param morphia The morphia mapper. Required.
   * @param mongo The mongo database connection. Required.
   * @param uri The mongo database uri. Required.
   * @return A Morphia {@link Datastore} for executing CRUD operations over
   *         POJOs.
   */
  @Bean
  public Datastore morphiaDatastore(final Morphia morphia, final Mongo mongo,
      final MongoURI uri) {
    Validate.notNull(morphia, "The morphia mapper is required.");
    Validate.notNull(mongo, "The mongo database connection is required.");
    Validate.notNull(uri, "The mongo database connection uri is required.");
    return morphia.createDatastore(mongo, uri.getDatabase());
  }
}
