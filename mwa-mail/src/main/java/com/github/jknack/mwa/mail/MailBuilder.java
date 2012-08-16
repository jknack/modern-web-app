package com.github.jknack.mwa.mail;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.EnumSet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Helper class for sending rich mail messages.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
public class MailBuilder {

  /**
   * Extended the {@link BufferedInputStream} by adding a content-type
   * attribute.
   *
   * @author edgar.espina
   * @since 0.1.3
   */
  private static class InputStreamWithContentType extends BufferedInputStream {
    /**
     * The content-type mark.
     */
    private String contentType;

    /**
     * Creates a new {@link InputStreamWithContentType}.
     *
     * @param in The {@link InputStream}.
     * @param contentType The content-type.
     */
    public InputStreamWithContentType(final InputStream in,
        final String contentType) {
      super(in);
      this.contentType = contentType;
    }

  }

  /**
   * What kind of embedded/inline types are required.
   *
   * @author edgar.espina@globant.com
   * @since 0.1.3
   */
  private static enum EmbeddedType {
    /**
     * Plain text.
     */
    DEFAULT(Object.class) {
      /**
       * {@inheritDoc}
       */
      @Override
      public void append(final MimeMessageHelper message, final String cid,
          final Object embedded, final String contentType)
          throws MessagingException {
        throw new UnsupportedOperationException();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Object apply(final Object value) {
        String candidate = String.valueOf(value);
        if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
          return UriComponentsBuilder.fromHttpUrl(candidate).build().encode()
              .toString();
        }
        return value;
      }
    },

    /**
     * Resource types.
     */
    RESOURCE(Resource.class) {
      /**
       * {@inheritDoc}
       */
      @Override
      public void append(final MimeMessageHelper message, final String id,
          final Object embedded, final String contentType)
          throws MessagingException {
        message.addInline(id, (Resource) embedded);
      }

    },

    /**
     * File types.
     */
    FILE(File.class) {
      /**
       * {@inheritDoc}
       */
      @Override
      public void append(final MimeMessageHelper message, final String id,
          final Object embedded, final String contentType)
          throws MessagingException {
        message.addInline(id, (File) embedded);
      }

    },

    /**
     * Input stream.
     */
    INPUT_STREAM(InputStream.class) {
      /**
       * {@inheritDoc}
       */
      @Override
      public void append(final MimeMessageHelper message, final String id,
          final Object embedded, final String contentType)
          throws MessagingException, IOException {
        message.addInline(id, toByteArrayResource((InputStream) embedded),
            contentType);
      }
    };

    /**
     * The embedded type.
     */
    private Class<?> embeddedType;

    /**
     * Creates a new {@link EmbeddedType}.
     *
     * @param embeddedType The embedded type.
     */
    private EmbeddedType(final Class<?> embeddedType) {
      this.embeddedType = embeddedType;
    }

    /**
     * Append the given resource to the provided message.
     *
     * @param message The message.
     * @param cid The resource id.
     * @param embedded The inline/embedded resource.
     * @param contentType The resource content-type.
     * @throws MessagingException If the embedded/inline resource cannot be
     *           added.
     */
    public abstract void append(MimeMessageHelper message, String cid,
        Object embedded, String contentType) throws MessagingException,
        IOException;

    /**
     * Pre-format the given value.
     *
     * @param value The value to be formatted.
     * @return A formatted value.
     */
    public Object apply(final Object value) {
      return cid + value;
    }

    /**
     * Return the type of the given embedded.
     *
     * @param embedded The embedded/inline resource.
     * @return The type of the given embedded.
     */
    public static EmbeddedType of(final Object embedded) {
      Validate.notNull(embedded, "Embedded object is required.");
      EnumSet<EmbeddedType> supported = EnumSet.allOf(EmbeddedType.class);
      supported.remove(EmbeddedType.DEFAULT);
      for (EmbeddedType embeddedValue : supported) {
        if (embeddedValue.embeddedType.isInstance(embedded)) {
          return embeddedValue;
        }
      }
      return EmbeddedType.DEFAULT;
    }
  }

  /**
   * The Spring mail message helper.
   */
  private MimeMessageHelper message;

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(MailBuilder.class);

  /**
   * The cid prefix for inline resources.
   */
  private static final String cid = "cid:";

  /**
   * Creates a new {@link MailBuilder}.
   *
   * @param message The mail mime-message.
   * @throws MessagingException If something goes wrong.
   */
  private MailBuilder(final MimeMessageHelper message)
      throws MessagingException {
    this.message = message;
  }

  /**
   * Set the given text directly as content.
   * Always applies the default content type "text/plain".
   * Optionally, you can send a message template and pass some argument for
   * merging and create the final message.
   * <p>
   * Usage:
   * </p>
   *
   * <pre>
   * text(&quot;Hello {0}!&quot;, &quot;World&quot;);
   * </pre>
   *
   * @param text The text for the message.
   * @return This {@link MailBuilder}.
   * @throws MessagingException in case of errors.
   * @see MessageFormat#format
   */
  public MailBuilder text(final String text, final Object... args)
      throws MessagingException {
    String formattedText = MessageFormat.format(text, processArgs(args));
    message.setText(formattedText);
    logger.debug("mail-body: {}", formattedText);
    return this;
  }

  /**
   * Set 'to' with the provided email addresses.
   *
   * @param to The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder to(final String... to) throws MessagingException {
    logger.debug("mail-to: {}", Joiner.on(", ").join(to));
    message.setTo(to);
    return this;
  }

  /**
   * Set 'to' with the provided email addresses.
   *
   * @param to The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder to(final Iterable<String> to) throws MessagingException {
    return to(Iterables.toArray(to, String.class));
  }

  /**
   * Set 'bcc' with the provided email addresses.
   *
   * @param bcc The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder bcc(final String... bcc) throws MessagingException {
    logger.debug("mail-bcc: {}", Joiner.on(",").join(bcc));
    message.setBcc(bcc);
    return this;
  }

  /**
   * Set 'bcc' with the provided email addresses.
   *
   * @param bcc The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder bcc(final Iterable<String> bcc) throws MessagingException {
    return bcc(Iterables.toArray(bcc, String.class));
  }

  /**
   * Set cc with the provided email addresses.
   *
   * @param cc The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder cc(final String... cc) throws MessagingException {
    logger.debug("mail-cc: {}", Joiner.on(",").join(cc));
    message.setCc(cc);
    return this;
  }

  /**
   * Set cc with the provided email addresses.
   *
   * @param cc The email addresses.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder cc(final Iterable<String> cc) throws MessagingException {
    return cc(Iterables.toArray(cc, String.class));
  }

  /**
   * Set 'from' with the provided email address.
   *
   * @param from The email address.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder from(final String from) throws MessagingException {
    logger.debug("mail-from: {}", from);
    message.setFrom(from);
    return this;
  }

  /**
   * Set 'from' with the provided email address.
   *
   * @param from The email address.
   * @param name The personal name.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder from(final String from, final String name)
      throws MessagingException, UnsupportedEncodingException {
    message.setFrom(from, name);
    logger.debug("mail-from: \"{}\" <{}>", name, from);
    return this;
  }

  /**
   * Set 'replayTo' with the provided email address.
   *
   * @param replayTo The email address.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder replyTo(final String replayTo) throws MessagingException {
    logger.debug("mail-replayTo: {}", replayTo);
    message.setReplyTo(replayTo);
    return this;
  }

  /**
   * Set 'replayTo' with the provided email address.
   *
   * @param replayTo The email address.
   * @param name The personal name.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder replyTo(final String replayTo, final String name)
      throws MessagingException, UnsupportedEncodingException {
    logger.debug("mail-replayTo: \"{}\" <{}>", name, replayTo);
    message.setReplyTo(replayTo, name);
    return this;
  }

  /**
   * Set 'subject' of the message.
   *
   * @param subject The email address.
   * @return This {@link MailBuilder}.
   * @throws MessagingException
   */
  public MailBuilder subject(final String subject) throws MessagingException {
    logger.debug("mail-subject: {}", subject);
    message.setSubject(subject);
    return this;
  }

  /**
   * Set the given text directly as content in non-multipart mode
   * or as default body part in multipart mode.
   * The "html" flag determines the content type to apply.
   * <p>
   * Usage:
   * </p>
   *
   * <pre>
   *  html("&lt;img src=\"{0}\"&gt;"&lt;p&gt;Hello {1}!&lt;/p&gt;,
   *    new File("header.jpg"),
   *    "World");
   * </pre>
   *
   * Or:
   *
   * <pre>
   *  html("&lt;img src=\"{0}\"&gt;"&lt;p&gt;Hello {1}!&lt;/p&gt;,
   *    MailBuilder.mailInputStream(source, "image/jpg"),
   *    "World");
   * </pre>
   *
   * @param html The html code as string.
   * @param args The embedded elements. Optional.
   * @return This {@link MailBuilder}.
   * @throws MessagingException If something goes wrong.
   * @throws IOException If the embedded/inline resource cannot be read it.
   */
  public MailBuilder html(final String html, final Object... args)
      throws MessagingException, IOException {
    // The order is important so we need to set the text first and append the
    // inline later.

    // Step 1: Calculate resource id (if any)
    Object[] msgArgs = processArgs(args);

    // Step 2: Set the text with the resource ids.
    String formattedText = MessageFormat.format(html, msgArgs);
    logger.debug("mail-body: {}", formattedText);
    message.setText(formattedText, true);

    // Step 3: add embedded/inline
    for (int i = 0; i < args.length; i++) {
      EmbeddedType embeddedType = EmbeddedType.of(args[i]);
      if (embeddedType != EmbeddedType.DEFAULT) {
        String contentType = null;
        if (args[i] instanceof InputStream) {
          contentType = contentTypeOf((InputStream) args[i]);
        }
        embeddedType.append(message,
            ((String) msgArgs[i]).substring(cid.length()), args[i],
            contentType);
      }
    }
    return this;
  }

  /**
   * Add an attachment to the MimeMessage, taking the content from a
   * <code>java.io.File</code>.
   * <p>
   * The content type will be determined by the name of the given content file.
   * Do not use this for temporary files with arbitrary filenames (possibly
   * ending in ".tmp" or the like)!
   *
   * @param name The attachment name.
   * @param file The resource to add.
   * @return This {@link MailBuilder}.
   * @throws MessagingException If something goes wrong.
   */
  public MailBuilder attach(final String name, final File file)
      throws MessagingException {
    logger.debug("mail-attachment: {}={}", name, file);
    message.addAttachment(name, file);
    return this;
  }

  /**
   * Add an attachment to the MimeMessage, taking the content from a
   * <code>java.io.File</code>.
   * <p>
   * The content type will be determined by the name of the given content file.
   * Do not use this for temporary files with arbitrary filenames (possibly
   * ending in ".tmp" or the like)!
   *
   * @param file The resource to add.
   * @return This {@link MailBuilder}.
   * @throws MessagingException If something goes wrong.
   */
  public MailBuilder attach(final File file) throws MessagingException {
    logger.debug("mail-attachment: {}", file);
    message.addAttachment(file.getName(), file);
    return this;
  }

  /**
   * Add an attachment to the MimeMessage.
   *
   * @param name The attachment name.
   * @param input The resource to add.
   * @param contentType The resource content type.
   * @return This {@link MailBuilder}.
   * @throws MessagingException If something goes wrong.
   * @throws IOException If something goes wrong.
   */
  public MailBuilder attach(final String name, final InputStream input,
      final String contentType) throws MessagingException, IOException {
    logger.debug("mail-attachment: {}={}", name, contentType);
    message.addAttachment(name, toByteArrayResource(input), contentType);
    return this;
  }

  /**
   * Add an attachment to the MimeMessage.
   *
   * @param name The attachment name.
   * @param resource The resource to add.
   * @param contentType The resource content type.
   * @return This {@link MailBuilder}.
   * @throws MessagingException If something goes wrong.
   */
  public MailBuilder attach(final String name, final Resource resource,
      final String contentType) throws MessagingException {
    logger.debug("mail-attachment: {}={}", name, contentType);
    message.addAttachment(name, resource, contentType);
    return this;
  }

  /**
   * Return a {@link MimeMessage} ready to be send.
   *
   * @return A {@link MimeMessage} fully configured.
   */
  public MimeMessage build() {
    return message.getMimeMessage();
  }

  /**
   * Creates a new mail message with multi-part support.
   *
   * @param sender The email sender. Required.
   * @return A new mail message with multi-part support.
   * @throws MessagingException If the message cannot be created.
   */
  public static MailBuilder newMail(final JavaMailSender sender)
      throws MessagingException {
    Validate.notNull(sender, "The email sender is required.");
    return new MailBuilder(new MimeMessageHelper(sender.createMimeMessage(),
        true));
  }

  /**
   * Creates a new simple mail message without multi-part support.
   *
   * @param sender The email sender. Required.
   * @return A new mail message without multi-part support.
   * @throws MessagingException If the message cannot be created.
   */
  public static MailBuilder newSimpleMail(final JavaMailSender sender)
      throws MessagingException {
    Validate.notNull(sender, "The email sender is required.");
    return new MailBuilder(new MimeMessageHelper(sender.createMimeMessage()));
  }

  /**
   * Detect the content-type of the {@link InputStream}.
   *
   * @param input The {@link InputStream} candidate.
   * @return The content-type. Default is: 'application/octet-stream'.
   */
  private static String contentTypeOf(final InputStream input) {
    if (input instanceof InputStreamWithContentType) {
      return ((InputStreamWithContentType) input).contentType;
    }
    return "application/octet-stream";
  }

  /**
   * Add content-type information to an {@link InputStream}.
   *
   * @param input The candidate input stream. Required.
   * @param contentType The content-type. Required.
   * @return An input-stream with content-type information.
   */
  public static InputStream mailInputStream(final InputStream input,
      final String contentType) {
    Validate.notNull(input, "The input stream is required.");
    Validate.notEmpty(contentType, "The content-type is required.");
    return new InputStreamWithContentType(input, contentType);
  }

  /**
   * Transform the input to a {@link ByteArrayResource}.
   *
   * @param input The source stream.
   * @throws IOException If something goes wrong.
   */
  private static ByteArrayResource toByteArrayResource(final InputStream input)
      throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    int b;
    while (-1 != (b = input.read())) {
      output.write(b);
    }
    return new ByteArrayResource(output.toByteArray());
  }

  /**
   * Process the list of arguments. There are special considerations for arg of
   * type: {@link File}, {@link Resource}, {@link InputStream} and Strings
   * starting with http:// or https://.
   *
   * @param args The argument to check.
   * @return A new list of arguments.
   */
  private static Object[] processArgs(final Object[] args) {
    Object[] result = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      EmbeddedType embeddedType = EmbeddedType.of(args[i]);
      result[i] =
          embeddedType.apply(embeddedType == EmbeddedType.DEFAULT ? args[i]
              : "embedded" + (i + 1));
    }
    return result;
  }
}
