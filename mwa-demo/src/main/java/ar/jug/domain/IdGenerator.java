package ar.jug.domain;

import java.math.BigInteger;

import org.apache.commons.lang3.Validate;

/**
 * Generate id based on some value.
 *
 * @author edgar.espina
 * @since 0.1
 */
public final class IdGenerator {
  /** The alphabet used to generate short URL codes. */
  private static final String alphabet =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  /** Length of the alphabet. */
  private static final BigInteger base = BigInteger.valueOf(alphabet.length());

  /**
   * This class cannot be instantiated.
   */
  private IdGenerator() {
  }

  /**
   * Transforms the seeds into a alphanumeric code.
   *
   * @param seed The seed used to generate codes.
   * @return The generated alphanumeric code.
   */
  public static String generate(final String seed) {
    Validate.notEmpty(seed, "The seed is required.");
    StringBuilder url = new StringBuilder();
    BigInteger i = new BigInteger(String.valueOf(Math.abs(seed.hashCode())));
    while (i.compareTo(BigInteger.ZERO) > 0) {
      url.append(alphabet.charAt(i.mod(base).intValue()));
      i = i.divide(base);
    }
    url.reverse();
    return url.toString();
  }
}
