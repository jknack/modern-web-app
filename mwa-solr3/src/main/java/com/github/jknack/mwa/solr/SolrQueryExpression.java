package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * Base class for build Solr queries.
 *
 * @author edgar.espina
 * @since 0.3.12
 * @see SolrQueryBuilder
 */
public abstract class SolrQueryExpression {

  /**
   * Return the Solr query string.
   *
   * @return The Solr query string.
   */
  public abstract String queryString();

  /**
   * Build a new {@link SolrQuery}.
   *
   * @return A new {@link SolrQuery}.
   */
  public SolrQuery build() {
    return new SolrQuery(queryString());
  }

  @Override
  public final String toString() {
    return queryString();
  }

  /**
   * Get an String version of the given expression.
   *
   * @param expression An expression.
   * @return A to string representation.
   */
  private String toString(final SolrQueryExpression expression) {
    String q = expression.queryString();
    if (isEmpty(q)) {
      return "";
    }
    if (expression instanceof SolrQueryTextExpression) {
      return q;
    }
    return "(" + q + ")";
  }

  /**
   * prefix the expression if necessary.
   *
   * @param expression The expression.
   * @param prefix The expression's prefix.
   * @return A string representation.
   */
  protected String prefix(final SolrQueryExpression expression, final String prefix) {
    String q = toString(expression);
    if (isEmpty(q)) {
      return "";
    }
    return prefix + q;
  }
}
