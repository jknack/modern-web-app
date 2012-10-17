# MWA Mail

## Introduction
It let you send rich email message through a simple and intuitive manner.

## Configuration
### Maven

```xml
<dependency>
  <groupId>com.github.jknack</groupId>
  <artifactId>mwa-mail</artifactId>
  <version>${mwa-version}</version>
</dependency>
```

### Required properties
* mail.smtp.host: Set the mail server host, typically an SMTP host.

#### Optional configuration
* mail.smtp.port: Set the mail server port. Default is: 25.

* mail.user: Set the username for the account at the mail host.

* mail.password: Set the password for the account at the mail host.

* mail.smtp.starttls.enable: Use TLS to encrypt communication with SMTP server.

* mail.smtp.sendpartial: If set to true, and this message has some valid and
some invalid addresses, send the message anyway, reporting the partial failure
with a SendFailedException. If set to true (the default).

* mail.smtp.from: Set the From address to appear in the SMTP envelope. Note
that this is different than the From address that appears in the message itself.
The envelope From address is typically used when reporting errors.
See [RFC 821](http://www.ietf.org/rfc/rfc821.txt) for details.

* mail.smtp.dsn.notify: Set notification options to be used if the server supports
Delivery Status Notification [RFC 1891](http://www.ietf.org/rfc/rfc1891.txt).
Either **NOTIFY_NEVER** or some combination of **NOTIFY_SUCCESS**, 
**NOTIFY_FAILURE**, and **NOTIFY_DELAY**.

* mail.smtp.dsn.ret: Set return option to be used if server supports
Delivery Status Notification [RFC 1891](http://www.ietf.org/rfc/rfc1891.txt).
**RETURN_FULL** or **RETURN_HDRS**.

* mail.smtp.allow8bitmime: If set to true, and the server supports the
8BITMIME extension, text parts of this message that use the "quoted-printable"
or "base64" encodings are converted to use "8bit" encoding if they follow the
RFC 2045 rules for 8bit text.

## Activating the module

```java
import com.github.jknack.mwa.mail.MailModule;
...
public class Main extends Startup {
  ...
  protected Class<?>[] modules() {
    return new Class<?>[] {..., MailModule.class };
  }
  ...
} 
```

## Usage

```java
import import org.springframework.mail.javamail.JavaMailSender;
import static com.github.jknack.mwa.mail.MailBuilder.newMail;
import javax.mail.internet.MimeMessage;
...
@Service
public class MailSender {
  private JavaMailSender sender;

  @Inject
  public MailSender(JavaMailSender sender) {
    this.sender = sender;
  }

  public void sendMail(String from, String to) {
    MimeMessage message =
    newMail(sender)
      .from(from)
      .to(to)
      .subject("Trying the mwa-mail system")
      .text("Hello {0}! This is just a test please ignore it!", to)
      .build();

    sender.send(message);
  }
}
```

## Sending a simple text message

```java
MimeMessage message =
newMail(sender)
  .from(from)
  .to(to)
  .subject("Trying the mwa-mail system")
  .text("Hello {0}!", "world")
  .build();

sender.send(message);
}
```

## Sending a html message

```java
MimeMessage message =
newMail(sender)
  .from(from)
  .to(to)
  .subject("Trying the mwa-mail system")
  .html("<img src=\"{0}\"><p>Hello {1}!</p>", new File("logo.png"), "world")
  .build();

sender.send(message);
}
```

### Using InputStreams

```java
InputStream input = ...
MimeMessage message =
newMail(sender)
  .from(from)
  .to(to)
  .subject("Trying the mwa-mail system")
  .html("<img src=\"{0}\"><p>Hello {1}!</p>", input, "world")
  .build();

sender.send(message);
}
```

### Using InputStreams with content-type
In the previous example the content-type is set to: 'application/octect-stream'. Here you can set a different content-type

```java
import static com.github.jknack.mwa.mail.MailBuilder.mailInputStream;
...
InputStream input = ...
MimeMessage message =
newMail(sender)
  .from(from)
  .to(to)
  .subject("Trying the mwa-mail system")
  .html("<img src=\"{0}\"><p>Hello {1}!</p>", mailInputStream(input, "image/x-png"), "world")
  .build();

sender.send(message);
}
```

## Attaching a files

```java
MimeMessage message =
newMail(sender)
  .from(from)
  .to(to)
  .subject("Trying the mwa-mail system")
  .text("Have a look to the attached files")
  .attach(new File("attachment.zip"))
  .build();

sender.send(message);
}
```

## Using GMail
You've to set the following properties:

```properties
# Set the mail server host, typically an SMTP host.
mail.smtp.host=smtp.gmail.com

# Set the mail server port.
mail.smtp.port=587

# Set the username for the account at the mail host.
mail.user=username@gmail.com

# Set the password for the account at the mail host.
mail.password=password

# Use TLS to encrypt communication with SMTP server.
mail.smtp.starttls.enable=true

```
