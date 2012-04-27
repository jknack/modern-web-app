package org.knowhow.mwa.mail;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.knowhow.mwa.mail.MailModule.SMTP_ALLOW_8BIT_MIME;
import static org.knowhow.mwa.mail.MailModule.SMTP_DSN_NOTIFY;
import static org.knowhow.mwa.mail.MailModule.SMTP_DSN_RET;
import static org.knowhow.mwa.mail.MailModule.SMTP_ENVELOP_FROM;
import static org.knowhow.mwa.mail.MailModule.SMTP_HOST;
import static org.knowhow.mwa.mail.MailModule.SMTP_SENDPARTIAL;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Unit test for {@link MailModule}.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
public class MailModuleTest {

  @Test
  public void mailSender() throws MessagingException, IOException {
    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty(SMTP_HOST)).andReturn("mail.server.com");
    expect(env.getProperty(SMTP_SENDPARTIAL, "true")).andReturn("true");
    expect(env.getProperty(SMTP_ALLOW_8BIT_MIME)).andReturn("");
    expect(env.getProperty(SMTP_DSN_NOTIFY)).andReturn(null);
    expect(env.getProperty(SMTP_DSN_RET)).andReturn(null);
    expect(env.getProperty(SMTP_ENVELOP_FROM)).andReturn(null);

    replay(env);

    JavaMailSender sender = new MailModule().mailSender(env);
    assertNotNull(sender);

    verify(env);
  }
}
