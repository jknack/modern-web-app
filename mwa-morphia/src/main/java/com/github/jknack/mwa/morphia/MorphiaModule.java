package com.github.jknack.mwa.morphia;

import static com.github.jknack.mwa.ApplicationConstants.APP_NAMESPACE;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Set;

import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.github.jknack.mwa.mongo.MongoModule;
import com.github.jmkgreen.morphia.AbstractEntityInterceptor;
import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolationException;
import com.mongodb.DBObject;
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
   * A JSR-303 entity interceptor.
   *
   * @author edgar.espina
   */
  private static final class Jsr303Interceptor extends
      AbstractEntityInterceptor {
    /**
     * The validator factory. Required.
     */
    private ValidatorFactory validatorFactory;

    /**
     * Creates a new {@link Jsr303Interceptor}.
     *
     * @param validatorFactory The validator factory. Required.
     */
    public Jsr303Interceptor(final ValidatorFactory validatorFactory) {
      this.validatorFactory =
          checkNotNull(validatorFactory, "The validator factory is required.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prePersist(final Object ent, final DBObject dbObj,
        final Mapper mapr) {
      @SuppressWarnings("rawtypes")
      final Set validate = validatorFactory.getValidator().validate(ent);
      if (!validate.isEmpty()) {
        throw new ConstraintViolationException(validate);
      }
    }
  }

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(MorphiaModule.class);

  /**
   * The local validator factory's bean.
   */
  @Autowired(required = false)
  private ValidatorFactory validationFactory;

  /**
   * Publish a {@link Morphia} POJOs mapper for Mongo datatabases.
   *
   * @param env The application's environment. Required.
   * @return A {@link Morphia} POJOs mapper for Mongo datatabases.
   */
  @Bean
  public Morphia morphia(final Environment env) {
    notNull(env, "The env is required.");
    String[] namespace = env.getProperty(APP_NAMESPACE, String[].class);
    Morphia morphia = new Morphia();
    for (String ns : namespace) {
      logger.debug("Adding pacakge: {}", ns);
      morphia.mapPackage(ns);
    }

    if (validationFactory != null) {
      morphia.getMapper().addInterceptor(
          new Jsr303Interceptor(validationFactory));
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
