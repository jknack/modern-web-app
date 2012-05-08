package com.github.edgarespina.mwa.mail;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Provide common mail features.
 *
 * @author edgar.espina
 * @since 0.1.3
 * @see MailBuilder
 */
@Configuration
public class MailModule {

  /**
   * If set to true, and this message has some valid and some invalid
   * addresses, send the message anyway, reporting the partial failure with
   * a SendFailedException. If set to true (the default).
   * If true, overrides the <code>mail.smtp.sendpartial</code> property.
   */
  public static final String SMTP_SENDPARTIAL = "mail.smtp.sendpartial";

  /**
   * Set the From address to appear in the SMTP envelope. Note that this
   * is different than the From address that appears in the message itself.
   * The envelope From address is typically used when reporting errors.
   * See <A HREF="http://www.ietf.org/rfc/rfc821.txt">RFC 821</A> for
   * details.
   * If set, overrides the <code>mail.smtp.from</code> property.
   */
  public static final String SMTP_ENVELOP_FROM = "mail.smtp.from";

  /**
   * Set the mail server host, typically an SMTP host. Required.
   */
  public static final String SMTP_HOST = "mail.smtp.host";

  /**
   * Set the mail server port. Default is: 25.
   */
  public static final String SMTP_PORT = "mail.smtp.port";

  /**
   * Set notification options to be used if the server supports
   * Delivery Status Notification
   * (<A HREF="http://www.ietf.org/rfc/rfc1891.txt">RFC 1891</A>).
   * Either <code>NOTIFY_NEVER</code> or some combination of
   * <code>NOTIFY_SUCCESS</code>, <code>NOTIFY_FAILURE</code>, and
   * <code>NOTIFY_DELAY</code>.
   * If set, overrides the <code>mail.smtp.dsn.notify</code> property.
   */
  public static final String SMTP_DSN_NOTIFY = "mail.smtp.dsn.notify";

  /**
   * Set return option to be used if server supports
   * Delivery Status Notification
   * (<A HREF="http://www.ietf.org/rfc/rfc1891.txt">RFC 1891</A>).
   * Either <code>RETURN_FULL</code> or <code>RETURN_HDRS</code>.
   * <p>
   * If set, overrides the <code>mail.smtp.dsn.ret</code> property.
   */
  public static final String SMTP_DSN_RET = "mail.smtp.dsn.ret";

  /**
   * If set to true, and the server supports the 8BITMIME extension, text
   * parts of this message that use the "quoted-printable" or "base64"
   * encodings are converted to use "8bit" encoding if they follow the
   * RFC 2045 rules for 8bit text.
   * <p>
   * If true, overrides the <code>mail.smtp.allow8bitmime</code> property.
   *
   * @param allow allow 8-bit flag
   */
  public static final String SMTP_ALLOW_8BIT_MIME = "mail.smtp.allow8bitmime";

  /**
   * Set the username for the account at the mail host, if any.
   */
  public static final String MAIL_USER = "mail.user";

  /**
   * Set the password for the account at the mail host, if any.
   */
  public static final String MAIL_PASSWORD = "mail.password";

  /**
   * Use SMTP-AUTH to authenticate to SMTP server. If {@link #MAIL_USER} is set
   * this property is to true.
   */
  public static final String SMTP_AUTH = "mail.smtp.auth";

  /**
   * Use TLS to encrypt communication with SMTP server.
   */
  public static final String SMTP_START_TLS = "mail.smtp.starttls.enable";

  /**
   * The mail system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(MailModule.class);

  /**
   * Publish a {@link JavaMailSender} into the application context.
   *
   * @param environment The application environment. Required.
   * @return A {@link JavaMailSender} service.
   */
  @Bean
  public JavaMailSender mailSender(final Environment environment) {
    Validate.notNull(environment, "The environment is required.");
    Properties properties = new Properties();
    String host = environment.getRequiredProperty(SMTP_HOST);
    // Required property
    properties.setProperty(SMTP_HOST, host);
    logger.debug("{}: {}", SMTP_HOST, host);

    // Optional properties
    setPropertyIfPresent(SMTP_PORT, environment, properties);

    // Optional with default value
    String sendPartial = environment.getProperty(SMTP_SENDPARTIAL, "true");
    properties.setProperty(SMTP_SENDPARTIAL, sendPartial);
    logger.debug("{}: {}", SMTP_SENDPARTIAL, sendPartial);
    setPropertyIfPresent(SMTP_ALLOW_8BIT_MIME, environment, properties);
    setPropertyIfPresent(SMTP_DSN_NOTIFY, environment, properties);
    setPropertyIfPresent(SMTP_DSN_RET, environment, properties);
    setPropertyIfPresent(SMTP_ENVELOP_FROM, environment, properties);
    setPropertyIfPresent(SMTP_START_TLS, environment, properties);

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    String user = environment.getProperty(MAIL_USER);
    if (StringUtils.isNotBlank(user)) {
      logger.debug("{}: {}", MAIL_USER, user);
      String password = environment.getRequiredProperty(MAIL_PASSWORD);
      logger.debug("{}: {}", MAIL_PASSWORD, password.replaceAll(".", "\\*"));
      // Add user and pass to the Java's Mail System.
      properties.setProperty(MAIL_USER, user);
      properties.setProperty(SMTP_AUTH, "true");
      logger.debug("{}: {}", SMTP_AUTH, properties.getProperty(SMTP_AUTH));
      properties.setProperty(MAIL_PASSWORD, password);
      // Set user and pass to the mail sender too.
      sender.setUsername(user);
      sender.setPassword(password);
    }
    sender.setJavaMailProperties(properties);
    return sender;
  }

  /**
   * Set the give property name only if is present (not null and not empty).
   *
   * @param propertyName The property's name.
   * @param source The property provider.
   * @param destination The property destination.
   */
  private void setPropertyIfPresent(final String propertyName,
      final Environment source, final Properties destination) {
    String propertyValue = source.getProperty(propertyName);
    if (StringUtils.isNotBlank(propertyValue)) {
      destination.setProperty(propertyName, propertyValue);
      logger.debug("{}: {}", propertyName, propertyValue);
    }
  }

}
