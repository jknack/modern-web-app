package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Build eq expressions.
 *
 * @author edgar.espina
 * @since 0.4.0
 */
class SolrQueryEqExpression extends SolrQueryExpression {

  /**
   * The field's name.
   */
  private String field;

  /**
   * The expression.
   */
  private SolrQueryExpression expression;

  /**
   * Creates a new {@link SolrQueryEqExpression}.
   *
   * @param field The field's name. Required.
   * @param expression The field's expression value. Required.
   */
  public SolrQueryEqExpression(final String field, final SolrQueryExpression expression) {
    this.field = notEmpty(field, "The field is required.");
    this.expression = notNull(expression, "The expression is required.");
  }

  @Override
  public String queryString() {
    return prefix(expression, field + ":");
  }

}
