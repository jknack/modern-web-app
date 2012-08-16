package com.github.jknack.mwa;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

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
public class ClassPathScanner {

  /**
   * The classes pattern.
   */
  private static final String DEFAULT_RESOURCE_PATTERN = "/*.class";

  /**
   * The classes set.
   */
  private Set<Class<?>> classes = null;

  /**
   * The lock.
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * The candidate packages.
   */
  private Set<String> packages = new LinkedHashSet<String>();

  /**
   * The resource pattern to use when scanning the classpath.
   * This value will be appended to each base package name.
   */
  private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

  /**
   * The type filters.
   */
  private final Set<TypeFilter> filters = new LinkedHashSet<TypeFilter>();

  /**
   * Creates a new {@link ClassPathScanner}.
   */
  public ClassPathScanner() {
  }

  /**
   * Add a package to the scanner.
   *
   * @param candidatePackage The candidate package. Required.
   * @return This scanner.
   */
  public ClassPathScanner addPackage(final String candidatePackage) {
    Validate.notEmpty(candidatePackage, "The package to scan is required.");
    packages.add(candidatePackage);
    return this;
  }

  /**
   * Add a package to the scanner.
   *
   * @param candidatePackage The candidate package. Required.
   * @return This scanner.
   */
  public ClassPathScanner addPackage(final Package candidatePackage) {
    addPackage(checkNotNull(candidatePackage,
        "The package to scan is required.").getName());
    return this;
  }

  /**
   * Add packages to the scanner.
   *
   * @param packages The candidate packages. Required.
   * @return This scanner.
   */
  public ClassPathScanner addPackages(final String... packages) {
    checkArgument(packages.length != 0,
        "The packages to scan are required.");
    for (String candidate : packages) {
      addPackage(candidate);
    }
    return this;
  }

  /**
   * Add packages to the scanner.
   *
   * @param packages The candidate packages. Required.
   * @return This scanner.
   */
  public ClassPathScanner addPackages(final Package... packages) {
    checkArgument(packages.length != 0,
        "The packages to scan are required.");
    for (Package candidate : packages) {
      addPackage(candidate);
    }
    return this;
  }

  /**
   * Enable recursive scanning.
   *
   * @return This scanner.
   */
  public ClassPathScanner includeSubPackages() {
    this.resourcePattern = "/**/*.class";
    return this;
  }

  /**
   * Append a type filter.
   *
   * @param filters The filter list. Required.
   * @return This scanner.
   */
  public ClassPathScanner addFilters(final TypeFilter... filters) {
    checkArgument(filters.length != 0, "The class filter are required.");
    for (TypeFilter filter : filters) {
      this.filters.add(filter);
    }
    return this;
  }

  /**
   * Scan and collect class resources.
   *
   * @return All the matching classes.
   */
  public Set<Class<?>> scan() {
    lock.readLock().lock();
    try {
      if (classes == null) {
        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
          // re-check state
          if (classes == null) {
            classes = new LinkedHashSet<Class<?>>();
            ClassLoader loader = getClass().getClassLoader();
            for (String classname : list()) {
              classes.add(loader.loadClass(classname));
            }
          }
          lock.readLock().lock();
        } finally {
          lock.writeLock().unlock();
        }
      }
      return classes;
    } catch (Exception ex) {
      throw new IllegalStateException("Fail during package scanning", ex);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Perform Spring-based scanning for classes.
   *
   * @return The matching class names.
   * @throws IOException If the disk fails.
   */
  private Set<String> list() throws IOException {
    Set<String> classes = new HashSet<String>();
    ResourcePatternResolver resolver =
        new PathMatchingResourcePatternResolver();
    MetadataReaderFactory readerFactory =
        new CachingMetadataReaderFactory(resolver);
    for (String candidate : packages) {
      String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
          + ClassUtils.convertClassNameToResourcePath(candidate)
          + resourcePattern;
      Resource[] resources = resolver.getResources(pattern);
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
    for (TypeFilter filter : filters) {
      if (filter.match(reader, readerFactory)) {
        return true;
      }
    }
    return false;
  }

}
