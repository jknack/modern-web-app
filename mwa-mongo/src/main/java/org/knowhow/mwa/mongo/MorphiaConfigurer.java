package org.knowhow.mwa.mongo;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.ClassUtils;

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
public class MorphiaConfigurer {

  /**
   * The classes pattern.
   */
  private static final String RESOURCE_PATTERN = "/*.class";

  /**
   * The Spring resource discover.
   */
  private ResourcePatternResolver resourcePatternResolver =
      new PathMatchingResourcePatternResolver();

  /**
   * The entity to look for.
   */
  private TypeFilter[] entityTypeFilters = new TypeFilter[] {
      new AnnotationTypeFilter(Entity.class, false),
      new AnnotationTypeFilter(Embedded.class, false) };

  /**
   * The entity set.
   */
  private final Set<Class<?>> entities;

  /**
   * Creates a new {@link MorphiaConfigurer}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public MorphiaConfigurer(final String... packagesToScan) throws Exception {
    checkArgument(packagesToScan.length != 0,
        "The package to scan are required.");
    this.entities = scan(packagesToScan);
  }

  /**
   * Creates a new {@link MorphiaConfigurer}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public MorphiaConfigurer(final Package... packagesToScan) throws Exception {
    checkArgument(packagesToScan.length != 0,
        "The package to scan are required.");
    String[] packageNames = new String[packagesToScan.length];
    for (int i = 0; i < packagesToScan.length; i++) {
      packageNames[i] = packagesToScan[i].getName();
    }
    this.entities = scan(packageNames);
  }

  /**
   * Scan and collect Morphia resources.
   *
   * @param packagesToScan The list of packages.
   * @return A list with classes.
   * @throws Exception If the packages cannot be detected.
   */
  private Set<Class<?>> scan(final String[] packagesToScan) throws Exception {
    Set<Class<?>> entities = new LinkedHashSet<Class<?>>();
    ClassLoader loader = getClass().getClassLoader();
    for (String entity : list(packagesToScan)) {
      entities.add(loader.loadClass(entity));
    }
    return entities;
  }

  /**
   * The package where the persistent resources are.
   *
   * @return The package where the persistent resources are.
   */
  public String[] getPackages() {
    // Note: the package scan is able to look for sub-packages.
    Set<String> packages = new LinkedHashSet<String>();
    for (Class<?> entity : entities) {
      packages.add(entity.getPackage().getName());
    }
    return packages.toArray(new String[packages.size()]);
  }

  /**
   * All the persistent classes.
   *
   * @return All the persistent classes.
   */
  public Set<Class<?>> getClasses() {
    return this.entities;
  }

  /**
   * Perform Spring-based scanning for entity classes.
   *
   * @param packagesToScan The candidate packages.
   * @return All the class names that match a Morphia entity.
   */
  private String[] list(final String[] packagesToScan) {
    Set<String> entities = new HashSet<String>();
    try {
      for (String candidate : packagesToScan) {
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + ClassUtils.convertClassNameToResourcePath(candidate)
            + RESOURCE_PATTERN;
        Resource[] resources = this.resourcePatternResolver
            .getResources(pattern);
        MetadataReaderFactory readerFactory =
            new CachingMetadataReaderFactory(
                this.resourcePatternResolver);
        for (Resource resource : resources) {
          if (resource.isReadable()) {
            MetadataReader reader = readerFactory
                .getMetadataReader(resource);
            String className = reader.getClassMetadata()
                .getClassName();
            if (matchesFilter(reader, readerFactory)) {
              entities.add(className);
            }
          }
        }
      }
    } catch (IOException ex) {
      throw new MappingException(
          "Failed to scan classpath for morphia classes", ex);
    }
    return entities.toArray(new String[entities.size()]);
  }

  /**
   * Check whether any of the configured entity type filters matches the
   * current class descriptor contained in the metadata
   * reader.
   *
   * @param reader The metadata reader.
   * @param readerFactory The metadata reader factory.
   * @return True if the current resources matches a Morphia entity.
   * @throws IOException If the disk fails.
   */
  private boolean matchesFilter(final MetadataReader reader,
      final MetadataReaderFactory readerFactory) throws IOException {
    if (this.entityTypeFilters != null) {
      for (TypeFilter filter : this.entityTypeFilters) {
        if (filter.match(reader, readerFactory)) {
          return true;
        }
      }
    }
    return false;
  }

}
