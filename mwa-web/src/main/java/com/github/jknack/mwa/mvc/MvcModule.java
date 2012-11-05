package com.github.jknack.mwa.mvc;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure {@link ModelContribution} extension point.
 *
 * @author edgar.espina
 * @since 0.1.8
 */
@Configuration
public class MvcModule {

  /**
   * Publish the {@link ModelContributionInterceptor} hook.
   *
   * @param contributions The list of model contributions.
   * @return A new {@link ModelContributionInterceptor} hook.
   */
  @Bean
  public ModelContributionInterceptor modelContributionInterceptor(
      final ModelContribution[] contributions) {
    return new ModelContributionInterceptor(Arrays.asList(contributions));
  }
}
