package com.github.jknack.mwa.jpa;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Load a set of test files in JSON format.
 *
 * @author edgar.espina
 *
 */
public final class JpaFixtures {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(JpaFixtures.class);

  /**
   * Not allowed.
   */
  private JpaFixtures() {
  }

  /**
   * Persist any entity found under the base directory.
   *
   * @param applicationContext The application's context. Required.
   * @param emf The entity manager factory. Required.
   * @param baseDir The base directory. Required.
   * @param classes The map with entities names and classes.
   */
  public static void load(final ApplicationContext applicationContext,
      final EntityManagerFactory emf, final String baseDir, final Map<String, Class<?>> classes) {
    notNull(applicationContext, "The applicationContext is required.");
    notNull(emf, "The entity manager factory is required.");
    notEmpty(baseDir, "The baseDir is required.");
    notNull(classes, "The classes are required.");

    EntityManager em = emf.createEntityManager();
    EntityTransaction trx = em.getTransaction();
    ObjectMapper mapper = new ObjectMapper();
    TypeFactory typeFactory = TypeFactory.defaultInstance();
    try {
      trx.begin();
      for (Entry<String, Class<?>> entry : classes.entrySet()) {
        String filename = ResourceUtils.CLASSPATH_URL_PREFIX + baseDir + "/" + entry.getKey()
            + ".json";
        Class<?> entityType = entry.getValue();
        Resource jsonFile = applicationContext.getResource(filename);
        if (jsonFile.exists()) {
          logger.info("Loading fixture: {}", filename);
          InputStream input = null;
          try {
            input = jsonFile.getInputStream();
            String json = IOUtils.toString(input).trim();
            JavaType javaType = typeFactory.constructType(entityType);
            if (json.startsWith("[")) {
              javaType = typeFactory.constructCollectionType(ArrayList.class, entityType);
            }
            Object object = mapper.readValue(json, javaType);
            if (object instanceof List) {
              @SuppressWarnings({"unchecked", "rawtypes" })
              List<Object> list = (List) object;
              for (Object obj : list) {
                em.persist(obj);
              }
            } else {
              em.persist(object);
            }
          } finally {
            if (input != null) {
              input.close();
            }
          }
        }
      }
      trx.commit();
    } catch (IOException ex) {
      trx.rollback();
      throw new IllegalStateException("Unable to load fixtures", ex);
    }
  }

}
