package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.StringTokenizer;

import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * Build text expressions.
 *
 * @author edgar.espina
 * @since 0.4.0
 */
class SolrQueryTextExpression extends SolrQueryExpression {

  /**
   * Internal representation of '*'.
   */
  static final char ASTERISK = '\uFDD0';

  /**
   * Internal representation of '?'.
   */
  static final char QUESTION = '\uFDD1';

  /**
   * ANY char.
   */
  static final SolrQueryExpression ANY = new SolrQueryTextExpression("*");

  /**
   * EMPTY expression.
   */
  static final SolrQueryExpression NONE = new SolrQueryTextExpression("");

  /**
   * The expression text.
   */
  private String text;

  /**
   * The expression text.
   *
   * @param text The expression text.
   */
  public SolrQueryTextExpression(final String text) {
    this.text = notNull(text, "The text is required.");
  }

  @Override
  public String queryString() {
    return text;
  }

  /**
   * Build a text expression and quoted and escape if necessary.
   *
   * @param text The text expression.
   * @return A new text expression.
   */
  public static SolrQueryExpression term(final String text) {
    if (text.equals("*")) {
      return ANY;
    }
    if (text.length() == 0) {
      return NONE;
    }
    boolean quote = new StringTokenizer(text).countTokens() > 1;
    final String safeChars;
    if (quote) {
      safeChars = "\"" + text + "\"";
    } else {
      safeChars = ClientUtils.escapeQueryChars(text);
    }
    return new SolrQueryTextExpression(safeChars);
  }

  /**
   * Build a wilcard expression and quoted and escape if necessary.
   *
   * @param text The text expression.
   * @return A new text expression.
   */
  public static SolrQueryExpression wilcard(final String text) {
    if (text.equals("*")) {
      return ANY;
    }
    if (text.length() == 0) {
      return NONE;
    }
    return new SolrQueryTextExpression(
        ClientUtils.escapeQueryChars(
            text
                .replace('*', ASTERISK)
                .replace('?', QUESTION)
            )
            .replace(ASTERISK, '*')
            .replace(QUESTION, '?'));
  }

  /**
   * Build a phrase expression and quoted and escape if necessary.
   *
   * @param text The text expression.
   * @return A new text expression.
   */
  public static SolrQueryExpression phrase(final String text) {
    return new SolrQueryTextExpression("\"" + text + "\"");
  }
}
