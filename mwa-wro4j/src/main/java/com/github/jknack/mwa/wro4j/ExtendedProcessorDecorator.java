package com.github.jknack.mwa.wro4j;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.tools.ant.taskdefs.ManifestTask.Mode;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;

import com.github.jknack.mwa.ApplicationModeAware;
import com.github.jknack.mwa.ApplicationMode;

/**
 * Add additional callback for {@link Environment}, {@link Mode} and
 * {@link UriLocatorFactory}.
 *
 * @author edgar.espina
 */
public class ExtendedProcessorDecorator extends ProcessorDecorator implements
    EnvironmentAware, ApplicationModeAware, UriLocatorFactoryAware {

  /**
   * A resource post processor with MWA callbacks.
   *
   * @author edgar.espina
   */
  private abstract static class ResourcePostProcessorWrapper implements
      ResourcePreProcessor, EnvironmentAware, ApplicationModeAware,
      UriLocatorFactoryAware {
  }

  /**
   * Create a new {@link ExtendedProcessorDecorator}.
   *
   * @param processor The target processor.
   */
  public ExtendedProcessorDecorator(final Object processor) {
    super(asPreProcessor(processor));
  }

  /**
   * Convert a processor as a {@link ResourcePreProcessor}.
   *
   * @param processor The target processor.
   * @return A {@link ResourcePreProcessor} version.
   */
  private static Object asPreProcessor(final Object processor) {
    if (processor instanceof ResourcePreProcessor) {
      return processor;
    } else if (processor instanceof ResourcePostProcessor) {
      return new ResourcePostProcessorWrapper() {
        @Override
        public void process(final Resource resource, final Reader reader,
            final Writer writer)
            throws IOException {
          ((ResourcePostProcessor) processor).process(reader, writer);
        }

        @Override
        public void setEnvironment(final Environment environment) {
          if (processor instanceof EnvironmentAware) {
            ((EnvironmentAware) processor).setEnvironment(environment);
          }
        }

        @Override
        public void setMode(final ApplicationMode mode) {
          if (processor instanceof ApplicationModeAware) {
            ((ApplicationModeAware) processor).setMode(mode);
          }
        }

        @Override
        public void setUriLocatorFactory(
            final UriLocatorFactory uriLocatorFactory) {
          if (processor instanceof UriLocatorFactoryAware) {
            ((UriLocatorFactoryAware) processor)
                .setUriLocatorFactory(uriLocatorFactory);
          }
        }

        @Override
        public String toString() {
          return processor.toString();
        }
      };
    }
    throw new IllegalArgumentException("Unknown processor: " + processor);
  }

  @Override
  public void setEnvironment(final Environment environment) {
    ResourcePreProcessor processor = getDecoratedObject();
    if (processor instanceof EnvironmentAware) {
      ((EnvironmentAware) processor).setEnvironment(environment);
    }
  }

  @Override
  public void setMode(final ApplicationMode mode) {
    ResourcePreProcessor processor = getDecoratedObject();
    if (processor instanceof ApplicationModeAware) {
      ((ApplicationModeAware) processor).setMode(mode);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    ResourcePreProcessor processor = getDecoratedObject();
    if (processor instanceof UriLocatorFactoryAware) {
      ((UriLocatorFactoryAware) processor)
          .setUriLocatorFactory(uriLocatorFactory);
    }
  }

}
