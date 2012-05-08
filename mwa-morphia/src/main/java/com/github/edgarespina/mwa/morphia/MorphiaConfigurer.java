package com.github.edgarespina.mwa.morphia;

import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.github.edgarespina.mwa.ClassPathScanner;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;

/**
 * <p>
 * Configures application mappings for Morphia persistence provider. It offers
 * the following functionality:
 * </p>
 * <ul>
 * <li>Scan for packages/sub-packages and collect classes with {@link Entity}
 * and {@link Embedded} classes.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MorphiaConfigurer extends ClassPathScanner {

  /**
   * Creates a new {@link MorphiaConfigurer}.
   */
  public MorphiaConfigurer() {
    addFilters(new AnnotationTypeFilter(Entity.class, false),
        new AnnotationTypeFilter(Embedded.class, false));
  }
}
