package org.knowhow.mwa.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

/**
 * <p>
 * A {@link HandlerExceptionResolver} that is enable to return message error
 * using a {@link HttpMessageConverter}.
 * </p>
 * The default message contains two fields:
 * <ol>
 * <li>type: with the short exception class name.
 * <li>message: with the exception message.
 * </ol>
 *
 * @author edgar.espina
 * @since 0.1
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public class MessageConverterHandlerExceptionResolver
    extends AbstractHandlerExceptionResolver {

  /**
   * The list of message converters.
   */
  private List<HttpMessageConverter<?>> messageConverters;

  /**
   * The exception type.
   */
  private Class<? extends Exception> exceptionClass;

  /**
   * A new {@link MessageConverterHandlerExceptionResolver}.
   *
   * @param exceptionClass The exception type. Required.
   */
  public MessageConverterHandlerExceptionResolver(
      @Nonnull final Class<? extends Exception> exceptionClass) {
    this.exceptionClass =
        checkNotNull(exceptionClass, "The exception class is required.");
    setWarnLogCategory(getClass().getName());
  }

  /**
   * Resolve the exception as a message if the ex is an instance of
   * {@link #exceptionClass}. {@inheritDoc}
   */
  @Override
  protected ModelAndView doResolveException(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler,
      final Exception ex) {
    try {
      if (exceptionClass.isInstance(ex)) {
        handle(convert(ex), request, response);
        return new ModelAndView();
      }
    } catch (Exception handlerException) {
      logger.warn("Handling of [" + ex.getClass().getName()
          + "] resulted in Exception", handlerException);
    }
    return null;
  }

  /**
   * Convert the exception to a user friendly message.
   *
   * @param exception The exception.
   * @return A user friendly message.
   * @see #asMap(Throwable)
   */
  protected Object convert(final Exception exception) {
    Throwable cause = ExceptionUtils.getRootCause(exception);
    if (cause == null) {
      cause = exception;
    }
    return asMap(cause);
  }

  /**
   * Convert the exception into a map a write two fields in it:
   * type: exception class name; and message: exception error.
   *
   * @param cause The exception.
   * @return A map message.
   */
  protected Map<String, Object> asMap(final Throwable cause) {
    Map<String, Object> error = new LinkedHashMap<String, Object>();
    error.put("type", cause.getClass().getSimpleName());
    error.put("message", cause.getMessage());
    return error;
  }

  /**
   * Prepare everything for writing the response.
   *
   * @param error The error object.
   * @param request The request.
   * @param response The response.
   * @throws IOException If the response cannot be writing.
   */
  private void handle(final Object error,
      final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    MediaType contentType = contentType(request);
    HttpOutputMessage output = new ServletServerHttpResponse(response);
    messageConverter(contentType, error, request).write(error, contentType,
        output);
  }

  /**
   * Extract content-type from the request.
   *
   * @param request The request.
   * @return A content-type.
   */
  private MediaType contentType(final HttpServletRequest request) {
    return new ServletServerHttpRequest(request).getHeaders().getContentType();
  }

  /**
   * Detect a message converter for the given content-type.
   *
   * @param contentType The content-type.
   * @param error The error object.
   * @param request The request.
   * @return A message converter.
   */
  private HttpMessageConverter messageConverter(final MediaType contentType,
      final Object error, final HttpServletRequest request) {
    for (HttpMessageConverter converter : messageConverters) {
      if (converter.canWrite(error.getClass(), contentType)) {
        return converter;
      }
    }
    throw new IllegalStateException("Message converter not found for: "
        + contentType);
  }

  /**
   * Set the list of message converters.
   *
   * @param messageConverters The message converter. Required.
   */
  public void setMessageConverters(
      final List<HttpMessageConverter<?>> messageConverters) {
    this.messageConverters =
        checkNotNull(messageConverters,
            "The message converter list is required.");
  }
}
