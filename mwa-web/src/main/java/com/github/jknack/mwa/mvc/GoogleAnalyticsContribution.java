package com.github.jknack.mwa.mvc;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ModelAndView;

/**
 * Publish the google analytics snippets as a model contribution.
 *
 * @author edgar.espina
 * @since 0.1.8
 */
public class GoogleAnalyticsContribution extends AbstractModelContribution {

  /**
   * The tracking code property.
   */
  public static final String TRACKING_CODE = "ga.trackingCode";

  /**
   * The google analytics snippet.
   */
  private static final String SNIPPET =
      "<script type='text/javascript'>\n"
          + "var _gaq = _gaq || [];\n"
          + "_gaq.push(['_setAccount', '%s']);\n"
          + "_gaq.push(['_trackPageview']);\n"
          + "(function() {\n"
          + "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n"
          + "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n"
          + "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n"
          + "})();\n"
          + "</script>\n";

  /**
   * The variable's name.
   */
  public static final String VAR_NAME = "googleAnalytics";

  /**
   * The google analytics snippet.
   */
  private String snippet;

  /**
   * The tracking code. Optional for dev.
   */
  private String trackingCode;

  /**
   * Creates a new {@link GoogleAnalyticsContribution}.
   *
   * @param env The environment. Required.
   */
  public GoogleAnalyticsContribution(final Environment env) {
    notNull(env, "The environment is required.");
    init(env.getProperty(TRACKING_CODE, ""));
  }

  /**
   * Creates a new {@link GoogleAnalyticsContribution}.
   *
   * @param trackingCode The tracking code. Optional for dev.
   */
  public GoogleAnalyticsContribution(final String trackingCode) {
    init(trackingCode);
  }

  /**
   * Initialize the tracking code and ga code snippet.
   *
   * @param trackingCode
   */
  private void init(final String trackingCode) {
    this.snippet =
        StringUtils.isBlank(trackingCode) ? "" : snippet(trackingCode);
    this.trackingCode = StringUtils.trimToNull(trackingCode);
  }

  /**
   * Build the Google Analytics snippet using the given tracking code.
   *
   * @param trackingCode The google analytics tracking code.
   * @return The Google Analytics snippet
   */
  private static String snippet(final String trackingCode) {
    return String.format(SNIPPET, trackingCode.trim());
  }

  @Override
  public void contribute(final HttpServletRequest request,
      final HttpServletResponse response, final ModelAndView modelAndView)
      throws IOException {
    logger.debug("Google analytics configuration");
    logger.debug("  tracking-code: {}", trackingCode);
    logger.debug("  snippet: {}", snippet);
    Map<String, Object> model = modelAndView.getModel();
    model.put(VAR_NAME, snippet);
  }

}
