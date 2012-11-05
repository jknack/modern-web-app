package com.github.jknack.mwa.mvc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * Publish one or more HTML templates as model attributes.
 * Given a template directory:
 *
 * <pre>
 *   /templates
 *     a.html
 *     b.html
 *     /module
 *       a.html
 * </pre>
 *
 * If you want to publish the content of this directory as Spring model
 * attributes, you need:
 *
 * <pre>
 *  new ModernViewResolver(
 *    new HtmlTemplates(&quot;/templates&quot;, &quot; html&quot;);
 *  );
 * </pre>
 *
 * From the view you can access to a template by:
 *
 * <pre>
 *   &lt;script type="text/html" id="superA"&gt;
 *      ${templates.a}
 *   &lt;/script&gt;
 * </pre>
 *
 * @author edgar.espina
 * @since 0.1
 */
public class HtmlTemplates extends AbstractModelContribution implements
    ServletContextAware {

  /**
   * Load templates from file system.
   *
   * @author edgar.espina
   * @since 0.5
   */
  private interface TemplateLoader {

    /**
     * Returns templates as map instance.
     *
     * @param directory Where we should look for? Required.
     * @param extension The template's extension. Required.
     * @return A map with all the templates.
     * @throws IOException If a file cannot be read it.
     */
    Map<String, Object> get(String directory, String extension)
        throws IOException;
  }

  /**
   * Load templates from the file system.
   *
   * @author edgar.espina
   * @since 0.5
   */
  private class DefaultTemplateLoader implements TemplateLoader {

    /**
     * The lookup strategy.
     */
    private ResourcePatternResolver resolver;

    /**
     * Creates a {@link DefaultTemplateLoader}.
     *
     * @param context The Servlet Context.
     */
    public DefaultTemplateLoader(final ServletContext context) {
      resolver = new PathMatchingResourcePatternResolver(
          new ServletContextResourceLoader(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> get(final String directory,
        final String extension) throws IOException {
      Map<String, Object> scope = new HashMap<String, Object>();
      Resource[] resources =
          resolver.getResources(directory + "/**/*." + extension);
      for (Resource resource : resources) {
        String[] path = relativePath(directory, resource);
        // Read content.
        String content = Resources.toString(resource.getURL(), Charsets.UTF_8);
        String filename = path[path.length - 1];
        String filenameNoExt =
            filename.substring(0,
                filename.length() - Files.getFileExtension(filename).length()
                    - 1);
        scope(scope, path).put(filenameNoExt, content);
      }
      return scope;
    }

    /**
     * Creates a relative path.
     *
     * @param directory The directory.
     * @param resource The resource.
     * @return A relative path.
     * @throws IOException If the URI cannot be detected.
     */
    private String[] relativePath(final String directory,
        final Resource resource) throws IOException {
      String fullpath = resource.getURI().getPath();
      final String separator = "/";
      String relativePath = fullpath
          .substring(fullpath.indexOf(directory) + separator.length());
      // Make it relative.
      Iterable<String> path = Splitter.on(separator)
          .omitEmptyStrings()
          .trimResults()
          .split(relativePath);
      return Iterables.toArray(path, String.class);
    }

    /**
     * Return correct scope for the given path.
     *
     * @param scope The root scope.
     * @param path The path of the template.
     * @return The correct scope for the given path.
     */
    private Map<String, Object> scope(final Map<String, Object> scope,
        final String[] path) {
      Map<String, Object> root = scope;
      for (int i = 0; i < path.length - 1; i++) {
        @SuppressWarnings("unchecked")
        Map<String, Object> child = (Map<String, Object>) root.get(path[i]);
        if (child == null) {
          child = new HashMap<String, Object>();
          root.put(path[i], child);
        }
        root = child;
      }
      return root;
    }
  }

  /**
   * Cache support for {@link TemplateLoader}.
   *
   * @author edgar.espina
   * @since 0.5
   */
  private class CacheTemplateLoader implements TemplateLoader {

    /**
     * Who is the man?
     */
    private TemplateLoader loader;

    /**
     * The cache.
     */
    private Map<String, Object> cache;

    /**
     * Creates a new {@link CacheTemplateLoader}.
     *
     * @param loader Who is the man?
     */
    public CacheTemplateLoader(final TemplateLoader loader) {
      this.loader = loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> get(final String directory,
        final String extension)
        throws IOException {
      if (cache == null) {
        cache = loader.get(directory, extension);
      }
      return cache;
    }
  }

  /**
   * Where are the templates?
   */
  private String directory;

  /**
   * How they looks?
   */
  private String extension;

  /**
   * The template loader.
   */
  private TemplateLoader loader;

  /**
   * Creates a new {@link HtmlTemplates} model contribution.
   *
   * @param directory Where are the templates (/temp, /templates, etc.)?
   * @param extension How they looks (html, mustache, etc.)?
   */
  public HtmlTemplates(final String directory, final String extension) {
    Assert.notNull(directory, "The template's directory is required.");
    Assert.isTrue(directory.length() > 0,
        "The template's directory is required.");
    Assert.notNull(extension, "The template's directory is required.");
    Assert.isTrue(extension.length() > 0,
        "The template's directory is required.");
    this.directory = directory;
    this.extension = extension;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void contribute(final HttpServletRequest request,
      final HttpServletResponse response, final ModelAndView modelAndView)
      throws IOException {
    modelAndView.getModel().putAll(loader.get(directory, extension));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setServletContext(final ServletContext context) {
    try {
      this.loader = new DefaultTemplateLoader(context);
      if (useCache()) {
        // Wrap the default loader and add cache support
        this.loader = new CacheTemplateLoader(this.loader);
        // hit and load the cache immediately
        this.loader.get(directory, extension);
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to load templates.", ex);
    }
  }
}
