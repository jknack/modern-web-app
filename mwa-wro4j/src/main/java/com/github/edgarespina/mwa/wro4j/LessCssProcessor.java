package com.github.edgarespina.mwa.wro4j;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;

/**
 * Another less css processor base on {@link LessCompiler}.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
@SupportedResourceType(ResourceType.CSS)
class LessCssProcessor implements ResourcePreProcessor {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(LessCssProcessor.class);

  /**
   * Creates a new {@link LessCssProcessor}.
   */
  public LessCssProcessor() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    final StopWatch stopWatch = new StopWatch();

    stopWatch.start("lessify");
    try {
      ServletContext servletContext = Context.get().getServletContext();
      String uri = resource.getUri();
      File path = new File(servletContext.getRealPath(uri));
      logger.trace("Resolving: {} to {}", uri, path);
      writer.write(new LessCompiler().compile(path));
    } catch (LessException ex) {
      throw new LessRuntimeException(LessCssError.of(
          resource.getUri(), ex.getCause()), ex.getCause());
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
      stopWatch.stop();
      logger.debug(stopWatch.prettyPrint());
    }
  }

}
