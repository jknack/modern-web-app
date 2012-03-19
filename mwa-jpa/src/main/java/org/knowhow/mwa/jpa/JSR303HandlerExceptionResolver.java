package org.knowhow.mwa.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knowhow.mwa.handler.MessageConverterHandlerExceptionResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Handle {@link MethodArgumentNotValidException} and print them using a
 * {@link HttpMessageConverter}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class JSR303HandlerExceptionResolver extends
    MessageConverterHandlerExceptionResolver {

  /**
   * Creates a new {@link JSR303HandlerExceptionResolver}.
   */
  public JSR303HandlerExceptionResolver() {
    super(MethodArgumentNotValidException.class);
  }

  /**
   * Use a custom format for {@link MethodArgumentNotValidException}.
   * <ol>
   *  <li>field: The field's name. Optional.
   *  <li>rejectedValue: The rejected value. Optional.
   *  <li>message: The error message.
   *  <li>type: The error type.
   *  <li>source: The root/contianer object.
   * </ol>
   * {@inheritDoc}
   */
  @Override
  protected Object convert(final Exception exception) {
    BindingResult bindingResult =
        ((MethodArgumentNotValidException) exception).getBindingResult();
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
      error.put("type", objectError.getCode());
      error.put("source", objectError.getObjectName());
      errors.add(error);
    }
    return errors;
  }

}
