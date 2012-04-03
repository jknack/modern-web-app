package org.knowhow.mwa.mongo;

import org.knowhow.mwa.ClassPathScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

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
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public MorphiaConfigurer(final String... packagesToScan) throws Exception {
    super(packagesToScan);
  }

  /**
   * Creates a new {@link MorphiaConfigurer}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public MorphiaConfigurer(final Package... packagesToScan) throws Exception {
    super(packagesToScan);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TypeFilter[] typeFilters() {
    return new TypeFilter[] {
        new AnnotationTypeFilter(Entity.class, false),
        new AnnotationTypeFilter(Embedded.class, false) };
  }
}
