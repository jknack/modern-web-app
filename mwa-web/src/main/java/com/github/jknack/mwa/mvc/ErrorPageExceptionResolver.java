package com.github.jknack.mwa.mvc;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.util.WebUtils;

/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} implementation that allows for
 * mapping exception class names to view names, either for a set of given handlers or for all
 * handlers in the DispatcherServlet.
 * <p>
 * Any exception is mapped to the {@link #setDefaultErrorView(String) view}. By default, the view's
 * name is: <code>error</code>. Custom pages for view can be registered using the
 * {@link #map(HttpStatus, String, Class...)} method.
 * </p>
 * <p>
 * An "error" object is added to the model. The error object contains these attributes:
 * <ul>
 * <li>statusCode: The HTTP Status Code</li>
 * <li>reasonPhrase: The HTTP Reason Phrase</li>
 * <li>message: The exception's message</li>
 * <li>type: The exception's name</li>
 * <li>stackTrace: The exception's stacktrace</li>
 * <li>exception: The root exception</li>
 * </ul>
 * </p>
 * <p>
 * Error views are analogous to error page JSPs, but can be used with any kind of exception
 * including any checked one, with fine-granular mappings for specific handlers.
 * </p>
 *
 * @author edgar.espina
 */
public class ErrorPageExceptionResolver extends AbstractHandlerExceptionResolver implements
    InitializingBean, ApplicationContextAware {

  /**
   * An error page.
   *
   * @author edgar.espina
   *
   */
  private static final class ErrorPage {

    /**
     * The page's name.
     */
    private final String page;

    /**
     * The HTTP Status.
     */
    private final int statusCode;

    /**
     * Creates a new {@link ErrorPage}.
     *
     * @param page The page's name.
     * @param status The page's status.
     */
    private ErrorPage(final String page, final int status) {
      this.page = page;
      statusCode = status;
    }

  }

  /**
   * Exception mappings.
   */
  private final Map<Class<?>, ErrorPage> exceptions = new LinkedHashMap<Class<?>, ErrorPage>();

  /**
   * Default error's view.
   */
  private String defaultErrorView = "error";

  /**
   * A prefix for view pages.
   */
  private String prefix = "";

  /**
   * Apply model contributions.
   */
  private ModelContributionInterceptor contributionInterceptor;

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) {
    contributionInterceptor = notNull(applicationContext, "The context is required.")
        .getBean(ModelContributionInterceptor.class);
  }

  /**
   * Set the name of the default error view. This view will be returned if no specific mapping was
   * found.
   * <p>
   * Default is none.
   *
   * @param defaultErrorView The default's error view name. Required.
   */
  public void setDefaultErrorView(final String defaultErrorView) {
    this.defaultErrorView = notEmpty(defaultErrorView, "The default error view is required.");
  }

  /**
   * Map the given exception to the status code.
   *
   * @param httpStatus The {@link HttpStatus} code. Required.
   * @param exceptionClasses The exception class. Required.
   * @return The page exception resolver.
   */
  public ErrorPageExceptionResolver map(final HttpStatus httpStatus,
      final Class<?>... exceptionClasses) {
    return map(httpStatus, defaultErrorView, exceptionClasses);
  }

  /**
   * Map the given exception to the status code.
   *
   * @param httpStatus The {@link HttpStatus} code. Required.
   * @param viewName The view's name. Required.
   * @param exceptionClasses The exception class. Required.
   * @return The page exception resolver.
   */
  public ErrorPageExceptionResolver map(final HttpStatus httpStatus, final String viewName,
      final Class<?>... exceptionClasses) {
    notNull(httpStatus, "The http status is required.");
    notEmpty(viewName, "The view's name is required.");
    notNull(exceptionClasses, "The exceptionClasses are required.");

    for (Class<?> exceptionClass : exceptionClasses) {
      isTrue(Exception.class.isAssignableFrom(exceptionClass), "Not an exception: "
          + exceptionClass);
      exceptions.put(exceptionClass, new ErrorPage(viewName, httpStatus.value()));
    }
    return this;
  }

  /**
   * Actually resolve the given exception that got thrown during on handler execution, returning a
   * ModelAndView that represents a specific error page if appropriate.
   * <p>
   * May be overridden in subclasses, in order to apply specific exception checks. Note that this
   * template method will be invoked <i>after</i> checking whether this resolved applies
   * ("mappedHandlers" etc), so an implementation may simply proceed with its actual exception
   * handling.
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler the executed handler, or <code>null</code> if none chosen at the time of the
   *        exception (for example, if multipart resolution failed)
   * @param ex the exception that got thrown during handler execution
   * @return a corresponding ModelAndView to forward to, or <code>null</code> for default processing
   */
  @Override
  protected ModelAndView doResolveException(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler, final Exception ex) {

    // Expose ModelAndView for chosen error view.
    Throwable targetException = ex;
    ErrorPage errorPage = findErrorPage(exceptions, targetException);
    if (errorPage == null) {
      // Try with root cause
      targetException = ExceptionUtils.getRootCause(targetException);
      if (targetException != null) {
        errorPage = findErrorPage(exceptions, targetException);
      }
      if (errorPage == null) {
        return null;
      }
    }
    applyStatusCodeIfPossible(request, response, errorPage.statusCode);
    ModelAndView modelAndView = newModelAndView(errorPage, targetException, request);
    try {
      contributionInterceptor.postHandle(request, response, handler, modelAndView);
    } catch (Exception iex) {
      logger.warn("Cannot apply model contributions", iex);
    }
    return modelAndView;
  }

  /**
   * Find a matching view name in the given exception mappings.
   *
   * @param exceptionMappings mappings between exception class names and error view names
   * @param ex the exception that got thrown during handler execution
   * @return the error page.
   */
  protected ErrorPage findErrorPage(final Map<Class<?>, ErrorPage> exceptionMappings,
      final Throwable ex) {
    int deepest = Integer.MAX_VALUE;
    ErrorPage errorPage = null;
    for (Entry<Class<?>, ErrorPage> exceptionMapping : exceptionMappings.entrySet()) {
      int depth = getDepth(exceptionMapping.getKey(), ex);
      if (depth >= 0 && depth < deepest) {
        deepest = depth;
        errorPage = exceptionMapping.getValue();
      }
    }
    if (errorPage == null) {
      return null;
    }
    logger.debug("Resolving: " + ex.getClass().getName() + " to HTTP Status: "
        + errorPage.statusCode);
    return errorPage;
  }

  /**
   * Return the depth to the superclass matching.
   * <p>
   * 0 means ex matches exactly. Returns -1 if there's no match. Otherwise, returns depth. Lowest
   * depth wins.
   *
   * @param exceptionMapping The exception to match for.
   * @param ex The current exception.
   * @return A deep level.
   */
  protected int getDepth(final Class<?> exceptionMapping, final Throwable ex) {
    return getDepth(exceptionMapping, ex.getClass(), 0);
  }

  /**
   * Return the depth to the superclass matching.
   * <p>
   * 0 means ex matches exactly. Returns -1 if there's no match. Otherwise, returns depth. Lowest
   * depth wins.
   *
   * @param exceptionMapping The exception to look for.
   * @param exceptionClass The current exception.
   * @param depth Deep level.
   * @return A deep level.
   */
  private int getDepth(final Class<?> exceptionMapping, final Class<?> exceptionClass,
      final int depth) {
    if (exceptionMapping == exceptionClass) {
      // Found it!
      return depth;
    }
    // If we've gone as far as we can go and haven't found it...
    if (exceptionClass == Throwable.class) {
      return -1;
    }
    return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
  }

  /**
   * Apply the specified HTTP status code to the given response, if possible (that is,
   * if not executing within an include request).
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param statusCode the status code to apply
   * @see #determineStatusCode
   * @see #setDefaultStatusCode
   * @see HttpServletResponse#setStatus
   */
  protected void applyStatusCodeIfPossible(final HttpServletRequest request,
      final HttpServletResponse response, final int statusCode) {
    if (!WebUtils.isIncludeRequest(request)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Applying HTTP status code " + statusCode);
      }
      response.setStatus(statusCode);
      request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
    }
  }

  /**
   * Return a ModelAndView for the given request, view name and exception.
   *
   * @param errorPage the name of the error view
   * @param ex the exception that got thrown during handler execution
   * @param request current HTTP request (useful for obtaining metadata)
   * @return the ModelAndView instance
   */
  protected ModelAndView newModelAndView(final ErrorPage errorPage, final Throwable ex,
      final HttpServletRequest request) {
    ModelAndView mv = new ModelAndView(prefix + errorPage.page);

    Map<String, Object> error = new HashMap<String, Object>();
    error.put("statusCode", errorPage.statusCode);
    error.put("reasonPhrase", HttpStatus.valueOf(errorPage.statusCode).getReasonPhrase());
    error.put("type", ex.getClass().getSimpleName());
    error.put("message", ex.getMessage());
    StringWriter stackTrace = new StringWriter();
    PrintWriter writer = new PrintWriter(stackTrace);
    ExceptionUtils.printRootCauseStackTrace(ex, writer);
    error.put("stackTrace", stackTrace.toString());

    mv.addObject("error", error);
    mv.addObject("exception", ex);
    logException((Exception) ex, request);
    return mv;
  }

  @Override
  protected void logException(final Exception ex, final HttpServletRequest request) {
    logger.debug(buildLogMessage(ex, request), ex);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    exceptions.put(NoSuchRequestHandlingMethodException.class,
        new ErrorPage(defaultErrorView, SC_NOT_FOUND));

    exceptions.put(HttpRequestMethodNotSupportedException.class,
        new ErrorPage(defaultErrorView, SC_METHOD_NOT_ALLOWED));

    exceptions.put(HttpMediaTypeNotSupportedException.class,
        new ErrorPage(defaultErrorView, SC_UNSUPPORTED_MEDIA_TYPE));

    exceptions.put(HttpMediaTypeNotAcceptableException.class,
        new ErrorPage(defaultErrorView, SC_NOT_ACCEPTABLE));

    exceptions.put(MissingServletRequestParameterException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));

    exceptions.put(ServletRequestBindingException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));

    exceptions.put(ConversionNotSupportedException.class,
        new ErrorPage(defaultErrorView, SC_INTERNAL_SERVER_ERROR));

    exceptions.put(TypeMismatchException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));

    exceptions.put(HttpMessageNotReadableException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));

    exceptions.put(HttpMessageNotWritableException.class,
        new ErrorPage(defaultErrorView, SC_INTERNAL_SERVER_ERROR));

    exceptions.put(MethodArgumentNotValidException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));

    exceptions.put(MissingServletRequestPartException.class,
        new ErrorPage(defaultErrorView, SC_BAD_REQUEST));
  }

  /**
   * This prefix will be prepend to every view's name. Useful if you want to add your error pages in
   * one specific folder or if they start with a common name.
   *
   * @param prefix A prefix to be prepend. Required.
   */
  public void setPrefix(final String prefix) {
    this.prefix = notEmpty(prefix, "The prefix is required.");
  }
}
