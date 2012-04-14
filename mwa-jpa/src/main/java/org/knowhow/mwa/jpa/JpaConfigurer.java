package org.knowhow.mwa.jpa;

import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.knowhow.mwa.ClassPathScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

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
public class JpaConfigurer extends ClassPathScanner implements
    PersistenceUnitPostProcessor {

  /**
   * Creates a new {@link JpaConfigurer}.
   */
  public JpaConfigurer() {
    addFilters(new AnnotationTypeFilter(Entity.class, false),
        new AnnotationTypeFilter(Embeddable.class, false),
        new AnnotationTypeFilter(MappedSuperclass.class, false));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessPersistenceUnitInfo(
      final MutablePersistenceUnitInfo pui) {
    Set<Class<?>> classes = scan();
    for (Class<?> entityClass : classes) {
      pui.addManagedClassName(entityClass.getName());
    }
  }
}
