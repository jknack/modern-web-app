package com.github.edgarespina.mwa.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * Convert {@link BindingResult} using the {@link HttpMessageConverter} service.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class BindHandlerExceptionResolver extends
    MessageConverterHandlerExceptionResolver {

  /**
   * Creates a new {@link BindHandlerExceptionResolver}.
   *
   * @param exceptionClass The exception class. Required.
   */
  public BindHandlerExceptionResolver(
      final Class<? extends Exception> exceptionClass) {
    super(exceptionClass);
  }

  /**
   * Creates a new {@link BindHandlerExceptionResolver}.
   */
  public BindHandlerExceptionResolver() {
    super(BindException.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Object convert(final Exception exception) {
    return convert(((BindException) exception).getBindingResult());
  }

  /**
   * Use a custom format for {@link BindingResult}.
   * <ol>
   * <li>field: The field's name. Optional.
   * <li>rejectedValue: The rejected value. Optional.
   * <li>message: The error message.
   * <li>type: The error type.
   * <li>source: The root/contianer object.
   * </ol>
   * {@inheritDoc}
   */
  protected Object convert(final BindingResult bindingResult) {
    List<ObjectError> bindingErrors = bindingResult.getAllErrors();
    List<Object> errors = new ArrayList<Object>(bindingErrors.size());
    for (ObjectError objectError : bindingErrors) {
      String field = null;
      Object rejectedValue = null;
      if (objectError instanceof FieldError) {
        FieldError fieldError = (FieldError) objectError;
        field = fieldError.getField();
        rejectedValue = fieldError.getRejectedValue();
      }
      Map<String, Object> error = new HashMap<String, Object>();
      error.put("field", field);
      error.put("rejectedValue", rejectedValue);
      error.put("message", (StringUtils.trimToEmpty(field) + " " + objectError
          .getDefaultMessage()).trim());
      error.put("type", StringUtils.uncapitalize(objectError.getCode()));
      error.put("source", objectError.getObjectName());
      errors.add(error);
    }
    return errors;
  }

}
