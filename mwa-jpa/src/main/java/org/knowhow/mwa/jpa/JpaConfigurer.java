package org.knowhow.mwa.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.knowhow.mwa.ClassPathScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
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
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public JpaConfigurer(final String... packagesToScan) throws Exception {
    super(packagesToScan);
  }

  /**
   * Creates a new {@link JpaConfigurer}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public JpaConfigurer(final Package... packagesToScan) throws Exception {
    super(packagesToScan);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TypeFilter[] typeFilters() {
    return new TypeFilter[] {
        new AnnotationTypeFilter(Entity.class, false),
        new AnnotationTypeFilter(Embeddable.class, false),
        new AnnotationTypeFilter(MappedSuperclass.class, false) };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessPersistenceUnitInfo(
      final MutablePersistenceUnitInfo pui) {
    for (Class<?> entityClass : getClasses()) {
      pui.addManagedClassName(entityClass.getName());
    }
  }
}
