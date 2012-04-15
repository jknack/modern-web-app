package org.knowhow.mwa.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.knowhow.mwa.ClassPathScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * <p>
 * Configures application mappings for JPA persistence provider. It offers the
 * following functionality:
 * </p>
 * <ul>
 * <li>Scan for packages/sub-packages and collect classes with {@link Entity},
 * {@link Embeddable} and {@link MappedSuperclass}.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
public class JpaConfigurer extends ClassPathScanner {

  /**
   * Creates a new {@link JpaConfigurer}.
   */
  public JpaConfigurer() {
    addFilters(new AnnotationTypeFilter(Entity.class, false),
        new AnnotationTypeFilter(Embeddable.class, false),
        new AnnotationTypeFilter(MappedSuperclass.class, false));
  }

}
