package com.github.edgarespina.mwa.jpa;

import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageConverter;

import com.github.edgarespina.mwa.handler.MessageConverterHandlerExceptionResolver;

/**
 * Report {@link DataAccessException} exceptions using a
 * {@link HttpMessageConverter}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class DataAccessHandlerExceptionResolver extends
    MessageConverterHandlerExceptionResolver {

  /**
   * Creates a new {@link DataAccessHandlerExceptionResolver}.
   */
  public DataAccessHandlerExceptionResolver() {
    super(DataAccessException.class);
  }

  /**
   * Print the inmediate cause (if present).
   * {@inheritDoc}
   */
  @Override
  protected Object convert(final Exception exception) {
    Throwable cause = exception.getCause();
    return asMap(cause == null ? exception : cause);
  }

}
