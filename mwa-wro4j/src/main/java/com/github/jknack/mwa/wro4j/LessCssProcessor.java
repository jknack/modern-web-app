package com.github.jknack.mwa.wro4j;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.StopWatch;

/**
 * Another less css processor base on {@link LessCompiler}.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
@SupportedResourceType(ResourceType.CSS)
public class LessCssProcessor implements ResourcePostProcessor {

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

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("lessify");
    Context context = Context.get();
    HttpServletRequest request = context.getRequest();
    String uri = request.getRequestURI();
    try {
      logger.debug("lessifying: {}", uri);
      LessCompiler less = new LessCompiler();
      String content = IOUtils.toString(reader);
      writer.write(less.compile(content));
    } catch (LessException ex) {
      throw new LessRuntimeException(LessCssError.of(
          uri, ex.getCause()), ex.getCause());
    } finally {
      // Rhino throws an exception when trying to exit twice. Make sure we don't
      // get any exception
      if (org.mozilla.javascript.Context.getCurrentContext() != null) {
        org.mozilla.javascript.Context.exit();
      }
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
      stopWatch.stop();
      logger.debug(stopWatch.prettyPrint());
    }
  }

}
