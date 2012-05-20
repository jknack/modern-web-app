package com.github.edgarespina.mwa.wro4j;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import com.github.edgarespina.mwa.Application;

/**
 * <p>
 * Intercept URI request and report any problems detected in JS or CSS
 * resources. If no problem is detected the interceptor does nothing.
 * </p>
 * NOTE: This component is enable in "dev" mode.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
@Profile(Application.DEV_NAME)
@Component
public class WroProblemReporterInterceptor extends HandlerInterceptorAdapter {

  /**
   * A response wrapper useful for sending wro-error message.
   *
   * @author edgar.espina
   * @since 0.1.3
   */
  private static class WroResponse implements HttpServletResponse {

    /**
     * A callback interface for handling HTTP errors.
     */
    private interface OnErrorCallback {

      /**
       * Called when a http error is detected.
       *
       * @throws Exception If something goes wrong.
       */
      void onError() throws Exception;
    }

    /**
     * The in-memory writer.
     */
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * The http response.
     */
    private HttpServletResponse response;

    /**
     * The content type.
     */
    private String contentType;

    /**
     * The http status.
     */
    private int status;

    /**
     * Creates a new {@link WroResponse}.
     *
     * @param response The response.
     * @param contentType The content type.
     */
    public WroResponse(final HttpServletResponse response,
        final String contentType) {
      this.response = response;
      this.contentType = contentType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() throws IOException {
      return new PrintWriter(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return new ServletOutputStream() {
        @Override
        public void write(final int b) throws IOException {
          out.write(b);
        }
      };
    }

    @Override
    public String getCharacterEncoding() {
      return "UTF-8";
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public void setCharacterEncoding(final String charset) {
    }

    @Override
    public void setContentLength(final int len) {
    }

    @Override
    public void setContentType(final String type) {
    }

    @Override
    public void setBufferSize(final int size) {
    }

    @Override
    public int getBufferSize() {
      return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
      return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLocale(final Locale loc) {
    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public void addCookie(final Cookie cookie) {
    }

    @Override
    public boolean containsHeader(final String name) {
      return response.containsHeader(name);
    }

    @Override
    public String encodeURL(final String url) {
      return response.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(final String url) {
      return response.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(final String url) {
      return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(final String url) {
      return encodeRedirectURL(url);
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException {
      handleStatus(sc, new OnErrorCallback() {
        @Override
        public void onError() throws Exception {
          response.sendError(sc, msg);
        }
      });
    }

    @Override
    public void sendError(final int sc) throws IOException {
      handleStatus(sc, new OnErrorCallback() {
        @Override
        public void onError() throws Exception {
          response.sendError(sc);
        }
      });
    }

    @Override
    public void sendRedirect(final String location) throws IOException {
    }

    @Override
    public void setDateHeader(final String name, final long date) {
    }

    @Override
    public void addDateHeader(final String name, final long date) {
    }

    @Override
    public void setHeader(final String name, final String value) {
    }

    @Override
    public void addHeader(final String name, final String value) {
    }

    @Override
    public void setIntHeader(final String name, final int value) {
    }

    @Override
    public void addIntHeader(final String name, final int value) {
    }

    @Override
    public void setStatus(final int sc) {
      handleStatus(sc, new OnErrorCallback() {
        @Override
        public void onError() throws Exception {
          PrintWriter writer = null;
          try {
            writer = response.getWriter();
            IOUtils.copy(new StringReader(out.toString()), writer);
            response.setStatus(status);
          } finally {
            IOUtils.closeQuietly(writer);
          }
        }
      });
    }

    /**
     * Handle the HTTP Status. If there was an error the
     * {@link OnErrorCallback#onError()} method will be executed.
     *
     * @param sc The http status.
     * @param callback The on-error callback.
     */
    private void handleStatus(final int sc, final OnErrorCallback callback) {
      this.status = sc;
      if (status >= HttpServletResponse.SC_BAD_REQUEST) {
        try {
          callback.onError();
        } catch (RuntimeException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new IllegalStateException("Unexpected error", ex);
        }
      }
    }

    @Override
    public void setStatus(final int sc, final String sm) {
      handleStatus(sc, new OnErrorCallback() {
        @Override
        public void onError() throws Exception {
          response.sendError(sc, sm);
        }
      });
    }

    @Override
    public int getStatus() {
      return status;
    }

    @Override
    public String getHeader(final String name) {
      return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(final String name) {
      return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
      return response.getHeaderNames();
    }

    /**
     * True if there are http errors.
     *
     * @return True if there are http errors.
     */
    public boolean hasErrors() {
      return status >= HttpServletResponse.SC_BAD_REQUEST;
    }

  }

  /**
   * The {@link WroModelFactory} service.
   */
  private WroFilter filter;

  /**
   * Track changes between resources.
   */
  private ConcurrentMap<String, String> changeSet =
      new ConcurrentHashMap<String, String>();

  /**
   * The {@link UriLocatorFactory} service.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Creates a new {@link WroProblemReporter}.
   *
   * @param filter The {@link WroModelFactory} service. Required.
   * @param uriLocatorFactory The {@link UriLocatorFactory} service.
   */
  @Inject
  public WroProblemReporterInterceptor(final WroFilter filter,
      final UriLocatorFactory uriLocatorFactory) {
    this.filter =
        checkNotNull(filter, "The wroFilter is required.");
    this.uriLocatorFactory =
        checkNotNull(uriLocatorFactory, "The uriLocatorFactory is required.");
  }

  /**
   * <p>
   * Intercept URI request and report any problems detected in JS or CSS
   * resources. If no problem is detected the interceptor does nothing.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public void postHandle(final HttpServletRequest request,
      final HttpServletResponse response, final Object handler,
      final ModelAndView modelAndView) throws Exception {
    if (modelAndView == null) {
      return;
    }
    Map<String, Object> model = modelAndView.getModel();
    /**
     * Prepare js and css resources.
     */
    List<Resource> resources = new ArrayList<Resource>();
    resources.addAll(resources(model, JavaScriptExporter.RESOURCES));
    resources.addAll(resources(model, CssExporter.RESOURCES));
    for (Resource resource : resources) {
      String uri = resource.getUri();
      String input = WroHelper.safeRead(uriLocatorFactory, resource);
      String hash = DigestUtils.md5DigestAsHex(input.getBytes());
      String prevHash = changeSet.get(uri);
      if (!hash.equals(prevHash)) {
        WroResponse wroResponse =
            new WroResponse(response, resource.getType().getContentType());
        filter.doFilter(wroRequest(request, resource), wroResponse, null);
        if (wroResponse.hasErrors()) {
          // An error has been detected stop immediately
          break;
        } else {
          changeSet.putIfAbsent(uri, hash);
        }
      }
    }
  }

  /**
   * Read a list of resources form the model.
   *
   * @param model The model.
   * @param resourceKey The resource's key.
   * @return A list of resources form the model.
   */
  private List<Resource> resources(final Map<String, Object> model,
      final String resourceKey) {
    @SuppressWarnings("unchecked")
    List<Resource> resources = (List<Resource>) model.get(resourceKey);
    if (resources == null) {
      return Collections.emptyList();
    }
    return resources;
  }

  /**
   * Wrap the request and change the requested uri.
   *
   * @param request The http request.
   * @param resource The new http uri.
   * @return A wrapped request.
   */
  private static HttpServletRequest wroRequest(
      final HttpServletRequest request, final Resource resource) {
    final String uri =
        FilenameUtils.removeExtension(resource.getUri()) + "."
            + resource.getType().name().toLowerCase();
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getRequestURI() {
        return uri;
      }
    };
  }
}
