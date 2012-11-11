package com.github.jknack.mwa.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.springframework.context.ApplicationContext;

import com.github.jknack.mwa.ComponentConfigurer;

/**
 * Configure a {@link CamelContext}. Clients can publish {@link CamelConfigurer} into the
 * application's context.
 * This is useful in cases where you need to override the default configuration or for adding
 * {@link RoutesBuilder}.
 * Note that if you only need to add {@link RoutesBuilder}, you can publish {@link RoutesBuilder}
 * instances into the {@link ApplicationContext}.
 *
 * @author edgar.espina
 * @see ApplicationContext
 * @see RoutesBuilder
 */
public interface CamelConfigurer extends ComponentConfigurer<CamelContext> {
}
