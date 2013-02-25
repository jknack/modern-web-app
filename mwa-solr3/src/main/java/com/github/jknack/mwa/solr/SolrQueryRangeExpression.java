package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * Build range expressions.
 *
 * @author edgar.espina
 * @since 0.4.0
 */
class SolrQueryRangeExpression extends SolrQueryExpression {

  /**
   * The from expression.
   */
  private String from;

  /**
   * The to expression.
   */
  private String to;

  /**
   * A new {@link SolrQueryRangeExpression}.
   *
   * @param from The from expression. Required.
   * @param to The to expression. Required.
   */
  public SolrQueryRangeExpression(final String from, final String to) {
    this.from = notEmpty(from, "The from is required.");
    this.to = notEmpty(to, "The to is required.");
  }

  @Override
  public String queryString() {
    return "[" + from + " TO " + to + "]";
  }

}
