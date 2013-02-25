package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * Build Solr binary expressions like 'OR', 'AND', etc.
 *
 * @author edgar.espina
 * @since 0.4.0
 */
class SolrQueryBinExpression extends SolrQueryExpression {

  /**
   * The bin operator.
   */
  private String operator;

  /**
   * The expressions to join.
   */
  private SolrQueryExpression[] expressions;

  /**
   * Creates a new {@link SolrQueryBinExpression}.
   *
   * @param operator The operator to use.
   * @param expressions The expressions list.
   */
  public SolrQueryBinExpression(final String operator, final SolrQueryExpression... expressions) {
    notEmpty(operator, "The operator is required.");
    notEmpty(expressions, "At least two expressions are required.");
    isTrue(expressions.length > 1, "At least two expressions are required.");
    this.operator = operator;
    this.expressions = expressions;
  }

  @Override
  public String queryString() {
    StringBuilder buffer = new StringBuilder();
    for (SolrQueryExpression expression : expressions) {
      String text = expression.queryString();
      if (text.length() > 0) {
        buffer.append(text).append(operator);
      }
    }
    if (buffer.length() > 0) {
      buffer.setLength(buffer.length() - operator.length());
      return "(" + buffer + ")";
    }
    return "";
  }

  /**
   * Return an OR expression.
   *
   * @param expressions The expression set. Required.
   * @return An OR expression.
   */
  public static SolrQueryBinExpression or(final SolrQueryExpression... expressions) {
    return new SolrQueryBinExpression(" OR ", expressions);
  }

  /**
   * Return an AND expression.
   *
   * @param expressions The expression set. Required.
   * @return An AND expression.
   */
  public static SolrQueryBinExpression and(final SolrQueryExpression... expressions) {
    return new SolrQueryBinExpression(" AND ", expressions);
  }

  /**
   * Return an optional expression.
   *
   * @param expressions The expression set. Required.
   * @return An optional expression.
   */
  public static SolrQueryBinExpression firstOf(final SolrQueryExpression... expressions) {
    return new SolrQueryBinExpression(" ", expressions);
  }
}
