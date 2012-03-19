package org.knowhow.mwa.mongo;

import java.net.UnknownHostException;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.google.common.base.Strings;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * <p>
 * Configure Mongo database resources:
 * </p>
 * <ul>
 * <li>It read the "db" property from the {@link Environment} and publish as a
 * {@link MongoURI}:
 * <li>A {@link Mongo} connection.
 * <li>A {@link MongoDbFactory} database factory.
 * <li>A Spring Data {@link MongoTemplate}.
 * </ul>
 * <p>
 * This module let you take full control of a Mongo DB by using POJOs through
 * the Spring Data {@link MongoTemplate}.
 * </p>
 * <p>
 * Note: For Morphia Support use the {@link MorphiaModule}.
 * </p>
 *
 * @author edgar.espina
 * @see 0.1
 * @see MorphiaModule
 */
@Configuration
public class MongoModule {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(MongoModule.class);

  /**
   * <p>
   * Publish a {@link MongoURI} by reading the "db" property from the
   * application environment.
   * </p>
   * Mongo URI format: <code>
   * mongodb://[username:password@]host1[:port1][,host2[:port2],...
   * [,hostN[:portN]]][/[database][?options]]
   * </code>
   *
   * @param environment The application environment. Required.
   * @return A {@link MongoURI uri}.
   * @throws UnknownHostException If the host connection is rejected.
   */
  @Bean
  public MongoURI mongoURI(final Environment environment)
      throws UnknownHostException {
    Validate.notNull(environment, "The application environment is required.");
    MongoURI uri = new MongoURI(environment.getRequiredProperty("db"));
    String noUserUri = uri.toString();
    int atSign = noUserUri.indexOf("@");
    if (atSign > 0) {
      noUserUri = MongoURI.MONGODB_PREFIX + noUserUri.substring(atSign + 1);
    }
    logger.info("Starting {}", noUserUri);
    return uri;
  }

  /**
   * Connect to a Mongo database connection using the give {@link MongoURI uri}.
   *
   * @param uri The mongo db uri. Required.
   * @return A Mongo database connection.
   * @throws UnknownHostException If the host connection is rejected.
   */
  @Bean
  public Mongo mongo(final MongoURI uri)
      throws UnknownHostException {
    Validate.notNull(uri, "The mongo database uri is required.");
    return uri.connect();
  }

  /**
   * Publish a {@link MongoDbFactory} service.
   *
   * @param uri The mongo db uri. Required.
   * @param mongo The mongo database connection. Required.
   * @return A {@link MongoDbFactory} service.
   */
  @Bean
  public MongoDbFactory mongoDbFactory(final MongoURI uri, final Mongo mongo) {
    Validate.notNull(uri, "The mongo database uri is required.");
    Validate.notNull(mongo, "The mongo database connection is required.");
    String username = uri.getUsername();
    char[] password = uri.getPassword();
    if (Strings.isNullOrEmpty(username)) {
      return new SimpleMongoDbFactory(mongo, uri.getDatabase());
    }
    return new SimpleMongoDbFactory(mongo, uri.getDatabase(),
        new UserCredentials(username, password == null ? null
            : String.valueOf(password)));
  }

  /**
   * Publish a {@link MongoTemplate} for POJOs support.
   *
   * @param mongoDbFactory The mongo database factory. Required.
   * @return A {@link MongoTemplate} for POJOs support.
   */
  @Bean
  public MongoTemplate mongoTemplate(final MongoDbFactory mongoDbFactory) {
    Validate.notNull(mongoDbFactory, "The mongo database factory is required.");
    return new MongoTemplate(mongoDbFactory);
  }

  /**
   * Translate {@link Mongo} database exception as Spring database exception if
   * possible.
   *
   * @return A Spring database exception translator.
   */
  @Bean
  public MongoExceptionTranslator mongoExceptionTranslator() {
    return new MongoExceptionTranslator();
  }
}
