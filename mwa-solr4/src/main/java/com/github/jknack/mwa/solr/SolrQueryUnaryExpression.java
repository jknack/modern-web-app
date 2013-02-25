package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Build a unary expression.
 *
 * @author edgar.espina
 * @since 0.4.0
 */
class SolrQueryUnaryExpression extends SolrQueryExpression {

  /**
   * The unary operator.
   */
  private String operator;

  /**
   * The expression.
   */
  private SolrQueryExpression expression;

  /**
   * Creates a new {@link SolrQueryUnaryExpression}.
   *
   * @param operator The unary operator. Required.
   * @param expression The expression. Required.
   */
  public SolrQueryUnaryExpression(final String operator, final SolrQueryExpression expression) {
    this.operator = notEmpty(operator, "The operator is required.");
    this.expression = notNull(expression, "The expression is required.");
  }

  @Override
  public String queryString() {
    return prefix(expression, operator);
  }

  /**
   * Build a NOT expression.
   *
   * @param expression The expression to be negated.
   * @return Build a NOT expression.
   */
  public static SolrQueryUnaryExpression not(final SolrQueryExpression expression) {
    return new SolrQueryUnaryExpression("NOT ", expression);
  }

  /**
   * Build a prohibit expression.
   *
   * @param expression The expression to be prohibited.
   * @return Build a prohibit expression.
   */
  public static SolrQueryUnaryExpression prohibit(final SolrQueryExpression expression) {
    return new SolrQueryUnaryExpression("-", expression);
  }

  /**
   * Build a require expression.
   *
   * @param expression The expression to require.
   * @return Build a require expression.
   */
  public static SolrQueryUnaryExpression required(final SolrQueryExpression expression) {
    return new SolrQueryUnaryExpression("+", expression);
  }
}
