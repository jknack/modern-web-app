package com.github.edgarespina.mwa.mail;

import static com.github.edgarespina.mwa.mail.MailModule.MAIL_PASSWORD;
import static com.github.edgarespina.mwa.mail.MailModule.MAIL_USER;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_ALLOW_8BIT_MIME;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_DSN_NOTIFY;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_DSN_RET;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_ENVELOP_FROM;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_HOST;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_PORT;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_SENDPARTIAL;
import static com.github.edgarespina.mwa.mail.MailModule.SMTP_START_TLS;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.easymock.Capture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.github.edgarespina.mwa.mail.MailModule;

/**
 * Unit test for {@link MailModule}.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MailModule.class, JavaMailSenderImpl.class })
public class MailModuleTest {

  @Test
  public void mailSender() throws Exception {
    Environment env = createMock(Environment.class);
    String host = "mail.server.com";
    String sendpartial = "true";

    expect(env.getRequiredProperty(SMTP_HOST)).andReturn(host);
    expect(env.getProperty(SMTP_SENDPARTIAL, sendpartial)).andReturn("true");
    expect(env.getProperty(SMTP_PORT)).andReturn("");
    expect(env.getProperty(SMTP_START_TLS)).andReturn(null);
    expect(env.getProperty(MAIL_USER)).andReturn(null);
    expect(env.getProperty(SMTP_ALLOW_8BIT_MIME)).andReturn("");
    expect(env.getProperty(SMTP_DSN_NOTIFY)).andReturn(null);
    expect(env.getProperty(SMTP_DSN_RET)).andReturn(null);
    expect(env.getProperty(SMTP_ENVELOP_FROM)).andReturn(null);

    JavaMailSenderImpl mailSender =
        PowerMock.createMockAndExpectNew(JavaMailSenderImpl.class);
    Capture<Properties> properties = new Capture<Properties>();
    mailSender.setJavaMailProperties(capture(properties));

    PowerMock.replay(JavaMailSenderImpl.class);
    replay(env, mailSender);

    JavaMailSender sender = new MailModule().mailSender(env);
    assertNotNull(sender);
    checkProperty(properties, SMTP_HOST, host);
    checkProperty(properties, SMTP_PORT, null);
    checkProperty(properties, SMTP_START_TLS, null);
    checkProperty(properties, MAIL_USER, null);
    checkProperty(properties, SMTP_ALLOW_8BIT_MIME, null);
    checkProperty(properties, SMTP_DSN_NOTIFY, null);
    checkProperty(properties, SMTP_DSN_RET, null);
    checkProperty(properties, SMTP_ENVELOP_FROM, null);
    checkProperty(properties, SMTP_SENDPARTIAL, sendpartial);

    verify(env, mailSender);
    PowerMock.verify(JavaMailSenderImpl.class);
  }

  @Test
  public void mailSenderWithAuth() throws Exception {
    String host = "smtp.gmail.com";
    String port = "587";
    String sendpartial = "true";
    String startTls = "true";
    String user = "user@gmail.com";
    String pass = "pass";

    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty(SMTP_HOST)).andReturn(host);
    expect(env.getProperty(SMTP_SENDPARTIAL, "true")).andReturn(sendpartial);
    expect(env.getProperty(SMTP_PORT)).andReturn(port);
    expect(env.getProperty(SMTP_START_TLS)).andReturn(startTls);
    expect(env.getProperty(MAIL_USER)).andReturn(user);
    expect(env.getRequiredProperty(MAIL_PASSWORD)).andReturn(pass);
    expect(env.getProperty(SMTP_ALLOW_8BIT_MIME)).andReturn("");
    expect(env.getProperty(SMTP_DSN_NOTIFY)).andReturn(null);
    expect(env.getProperty(SMTP_DSN_RET)).andReturn(null);
    expect(env.getProperty(SMTP_ENVELOP_FROM)).andReturn(null);

    JavaMailSenderImpl mailSender =
        PowerMock.createMockAndExpectNew(JavaMailSenderImpl.class);
    Capture<Properties> properties = new Capture<Properties>();
    mailSender.setJavaMailProperties(capture(properties));
    mailSender.setUsername(user);
    expectLastCall();
    mailSender.setPassword(pass);
    expectLastCall();

    PowerMock.replay(JavaMailSenderImpl.class);
    replay(env, mailSender);

    JavaMailSender sender = new MailModule().mailSender(env);
    assertNotNull(sender);
    checkProperty(properties, SMTP_HOST, host);
    checkProperty(properties, SMTP_PORT, port);
    checkProperty(properties, SMTP_START_TLS, startTls);
    checkProperty(properties, MAIL_USER, user);
    checkProperty(properties, MAIL_PASSWORD, pass);
    checkProperty(properties, SMTP_ALLOW_8BIT_MIME, null);
    checkProperty(properties, SMTP_DSN_NOTIFY, null);
    checkProperty(properties, SMTP_DSN_RET, null);
    checkProperty(properties, SMTP_ENVELOP_FROM, null);
    checkProperty(properties, SMTP_SENDPARTIAL, sendpartial);

    verify(env, mailSender);
    PowerMock.verify(JavaMailSenderImpl.class);
  }

  private void checkProperty(final Capture<Properties> properties,
      final String name, final String value) {
    checkProperty(properties.getValue(), name, value);
  }

  private void checkProperty(final Properties properties, final String name,
      final String value) {
    assertEquals(properties.getProperty(name), value);
  }
}
