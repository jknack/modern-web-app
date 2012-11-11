package com.github.jknack.mwa;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configure a {@link ObjectMapper}. Please note this {@link ObjectMapper} is the one used for
 * Spring MVC.
 *
 * @author edgar.espina
 *
 */
public interface ObjectMapperConfigurer extends ComponentConfigurer<ObjectMapper> {
}
