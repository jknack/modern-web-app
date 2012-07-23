package com.github.edgarespina.mwa.wro4j;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;

import com.github.edgarespina.mwa.Mode;
import com.github.edgarespina.mwa.ModeAware;

/**
 * Add additional callback for {@link Environment}, {@link Mode} and
 * {@link UriLocatorFactory}.
 *
 * @author edgar.espina
 */
public class ExtendedProcessorDecorator extends ProcessorDecorator implements
    EnvironmentAware, ModeAware, UriLocatorFactoryAware {

  /**
   * A resource post processor with MWA callbacks.
   *
   * @author edgar.espina
   */
  private abstract static class ResourcePostProcessorWrapper implements
      ResourcePreProcessor, EnvironmentAware, ModeAware,
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
        public void setMode(final Mode mode) {
          if (processor instanceof ModeAware) {
            ((ModeAware) processor).setMode(mode);
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
  public void setMode(final Mode mode) {
    ResourcePreProcessor processor = getDecoratedObject();
    if (processor instanceof ModeAware) {
      ((ModeAware) processor).setMode(mode);
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
