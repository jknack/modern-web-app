package com.github.edgarespina.mwa.mail;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.github.edgarespina.mwa.mail.MailBuilder;
import com.google.common.collect.Lists;

/**
 * Unit test for {@link MailBuilder}.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MimeMessageHelper.class, MailBuilder.class })
public class MailBuilderTest {

  private static abstract class MailTestCase {
    public abstract void expectations(MimeMessageHelper message)
        throws Exception;

    public void run() throws Exception {
      MimeMessage mimeMessage = createMock(MimeMessage.class);

      MimeMessageHelper message = PowerMock
          .createMockAndExpectNew(MimeMessageHelper.class, mimeMessage, true);
      expectations(message);

      JavaMailSender sender = createMock(JavaMailSender.class);
      expect(sender.createMimeMessage()).andReturn(mimeMessage);

      PowerMock.replay(MimeMessageHelper.class);
      replay(sender, mimeMessage, message);

      MailBuilder mail = MailBuilder.newMail(sender);
      run(mail);

      verify(sender, mimeMessage, message);
      PowerMock.verify(MimeMessageHelper.class);
    }

    public abstract void run(MailBuilder mail) throws Exception;
  }

  @Test
  public void attachFile() throws Exception {
    new MailTestCase() {
      private File textFile = new File("text.txt");

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.attach(textFile);
        mail.attach("test.xml", textFile);
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.addAttachment("text.txt", textFile);
        message.addAttachment("test.xml", textFile);
      }
    }.run();
  }

  @Test
  public void attachInputStream() throws Exception {
    new MailTestCase() {
      private InputStream input = new ByteArrayInputStream("bytes".getBytes());

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.attach("attachment.f", input, "application/f");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.addAttachment("attachment.f",
            new ByteArrayResource("bytes".getBytes()), "application/f");
      }
    }.run();
  }

  @Test
  public void attachResource() throws Exception {
    new MailTestCase() {
      private Resource input = createMock(Resource.class);

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.attach("attachment.r", input, "application/r");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.addAttachment("attachment.r", input, "application/r");
      }
    }.run();
  }

  @Test
  public void bcc() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.bcc(Lists.newArrayList("one@mail.com", "two@mail.com"));
        mail.bcc("3@mail.com", "4@mail.com");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setBcc(aryEq(new String[] {"one@mail.com", "two@mail.com" }));
        expectLastCall();
        message.setBcc(aryEq(new String[] {"3@mail.com", "4@mail.com" }));
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void cc() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.cc(Lists.newArrayList("one@mail.com", "two@mail.com"));
        mail.cc("3@mail.com", "4@mail.com");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setCc(aryEq(new String[] {"one@mail.com", "two@mail.com" }));
        expectLastCall();
        message.setCc(aryEq(new String[] {"3@mail.com", "4@mail.com" }));
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void from() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.from("john.doe@mail.com");
        mail.from("john.doe@mail.com", "John Doe");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setFrom("john.doe@mail.com");
        expectLastCall();
        message.setFrom("john.doe@mail.com", "John Doe");
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void replyTo() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.replyTo("john.doe@mail.com");
        mail.replyTo("john.doe@mail.com", "John Doe");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setReplyTo("john.doe@mail.com");
        expectLastCall();
        message.setReplyTo("john.doe@mail.com", "John Doe");
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void to() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.to(Lists.newArrayList("to1@mail.com", "to2@mail.com"));
        mail.to("to3@mail.com", "to4@mail.com");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setTo(aryEq(new String[] {"to1@mail.com", "to2@mail.com" }));
        expectLastCall();
        message.setTo(aryEq(new String[] {"to3@mail.com", "to4@mail.com" }));
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void subject() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.subject("Test subject");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setSubject("Test subject");
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void text() throws Exception {
    new MailTestCase() {

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.text("Simple");
        mail.text("Hello {0}!", "John Doe");
        mail.text("Hello {0} {1}!", "John", "Doe");
        mail.text("Hi {0}, {1} sent you this link {2}", "John",
            "Juan", "http://link.to.com/some where");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setText("Simple");
        expectLastCall();
        message.setText("Hello John Doe!");
        expectLastCall();
        message.setText("Hello John Doe!");
        expectLastCall();
        message
            .setText("Hi John, Juan sent you this link http://link.to.com" +
                "/some%20where");
        expectLastCall();
      }
    }.run();
  }

  @Test
  public void html() throws Exception {
    new MailTestCase() {
      File header = new File("fakeHeader.jpg");

      @Override
      public void run(final MailBuilder mail) throws Exception {
        mail.html("<img src=\"{0}\"><p>Hello {1}!</p>", header, "John Doe");
      }

      @Override
      public void expectations(final MimeMessageHelper message)
          throws Exception {
        message.setText("<img src=\"cid:embedded1\"><p>Hello John Doe!</p>",
            true);
        expectLastCall();
        message.addInline("embedded1", header);
        expectLastCall();
      }
    }.run();
  }
}
