package org.knowhow.mwa;

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
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Sets;

/**
 * <p>
 * A classpath scanner It offers the following functionality:
 * </p>
 * <ul>
 * <li>Scan for packages/sub-packages and collect classes.
 * </ul>
 *
 * @author edgar.espina
 * @since 0.1
 */
public abstract class ClassPathScanner {

  /**
   * The classes pattern.
   */
  private static final String RESOURCE_PATTERN = "/*.class";

  /**
   * The recursive classes pattern.
   */
  private static final String RECURSIVE_RESOURCE_PATTERN = "**/*.class";

  /**
   * The Spring resource discover.
   */
  private ResourcePatternResolver resourcePatternResolver =
      new PathMatchingResourcePatternResolver();

  /**
   * The classes set.
   */
  private final Set<Class<?>> classes;

  /**
   * The resource pattern.
   */
  private String resourcePattern = RESOURCE_PATTERN;

  /**
   * Creates a new {@link ClassPathScanner}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public ClassPathScanner(final String... packagesToScan) throws Exception {
    checkArgument(packagesToScan.length != 0,
        "The package to scan are required.");
    this.classes = scan(packagesToScan);
  }

  /**
   * Creates a new {@link ClassPathScanner}.
   *
   * @param packagesToScan The packages to scan. Required.
   * @throws Exception If the packages cannot be detected.
   */
  public ClassPathScanner(final Package... packagesToScan) throws Exception {
    checkArgument(packagesToScan.length != 0,
        "The package to scan are required.");
    String[] packageNames = new String[packagesToScan.length];
    for (int i = 0; i < packagesToScan.length; i++) {
      packageNames[i] = packagesToScan[i].getName();
    }
    this.classes = scan(packageNames);
  }

  /**
   * Scan sub-packages.
   *
   * @return This {@link ClassPathScanner}.
   */
  public ClassPathScanner includeSubPackages() {
    resourcePattern = RECURSIVE_RESOURCE_PATTERN;
    return this;
  }

  /**
   * Scan and collect class resources.
   *
   * @param packagesToScan The list of packages.
   * @return All the matching classes.
   * @throws Exception If the packages cannot be detected.
   */
  private Set<Class<?>> scan(final String[] packagesToScan) throws Exception {
    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    ClassLoader loader = getClass().getClassLoader();
    for (String classname : list(packagesToScan)) {
      classes.add(loader.loadClass(classname));
    }
    return classes;
  }

  /**
   * The package list.
   *
   * @return The package list.
   */
  public Set<String> getPackages() {
    Set<String> packages = new LinkedHashSet<String>();
    for (Class<?> klass : classes) {
      packages.add(klass.getPackage().getName());
    }
    return packages;
  }

  /**
   * The types to look for.
   *
   * @return Thes type to look for.
   */
  protected abstract TypeFilter[] typeFilters();

  /**
   * All the matching classes.
   *
   * @return All the matching classes.
   */
  public Set<Class<?>> getClasses() {
    return this.classes;
  }

  /**
   * Perform Spring-based scanning for classes.
   *
   * @param packagesToScan The candidate packages.
   * @return The matching class names.
   * @throws IOException If the disk fails.
   */
  private Set<String> list(final String[] packagesToScan) throws IOException {
    Set<String> classes = new HashSet<String>();
    for (String candidate : Sets.newHashSet(packagesToScan)) {
      String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
          + ClassUtils.convertClassNameToResourcePath(candidate)
          + resourcePattern;
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
            classes.add(className);
          }
        }
      }
    }
    return classes;
  }

  /**
   * Check whether any of the configured type filters matches the
   * current class descriptor contained in the metadata
   * reader.
   *
   * @param reader The metadata reader.
   * @param readerFactory The metadata reader factory.
   * @return True if the current resources matches the filter.
   * @throws IOException If the disk fails.
   */
  private boolean matchesFilter(final MetadataReader reader,
      final MetadataReaderFactory readerFactory) throws IOException {
    for (TypeFilter filter : typeFilters()) {
      if (filter.match(reader, readerFactory)) {
        return true;
      }
    }
    return false;
  }

}
