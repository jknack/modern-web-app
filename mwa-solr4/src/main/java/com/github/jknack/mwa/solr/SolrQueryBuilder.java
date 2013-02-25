package com.github.jknack.mwa.solr;

import static org.apache.commons.lang3.Validate.notNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.solr.common.util.DateUtil;

import com.google.common.base.Function;

/**
 * Help to create Solr query using boolean clauses.
 *
 * @author edgar.espina
 * @since 0.3.11
 */
public final class SolrQueryBuilder {

  /**
   * Denied!
   */
  private SolrQueryBuilder() {
  }

  /**
   * Return a '*' expression.
   *
   * @return A '*' expression.
   */
  public static SolrQueryExpression any() {
    return SolrQueryTextExpression.ANY;
  }

  /**
   * An empty expression.
   *
   * @return An empty expression.
   */
  public static SolrQueryExpression none() {
    return SolrQueryTextExpression.NONE;
  }

  /**
   * Select the first expression if predicate is true.
   *
   * @param predicate The predicate value.
   * @param thenExpression Selected if predicate is true.
   * @param elseExpression Selected if predicate is false.
   * @return One of two expression.
   */
  public static SolrQueryExpression ternary(final boolean predicate,
      final SolrQueryExpression thenExpression, final SolrQueryExpression elseExpression) {
    return predicate ? thenExpression : elseExpression;
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Date from. Can't be null.
   * @param to Date to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final Date from, final Date to) {
    DateFormat dateFormat = DateUtil.getThreadLocalDateFormat();
    return range(dateFormat.format(from), dateFormat.format(to));
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Date from. Can't be null.
   * @param to Date to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final String from, final Date to) {
    DateFormat dateFormat = DateUtil.getThreadLocalDateFormat();
    return range(from, dateFormat.format(to));
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Date from. Can't be null.
   * @param to Date to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final Date from, final String to) {
    DateFormat dateFormat = DateUtil.getThreadLocalDateFormat();
    return range(dateFormat.format(from), to);
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Value from. Can't be null.
   * @param to Value to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final long from, final long to) {
    return range(Long.toString(from), Long.toString(to));
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Value from. Can't be null.
   * @param to Value to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final String from, final long to) {
    return range(from, Long.toString(to));
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Value from. Can't be null.
   * @param to Value to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final long from, final String to) {
    return range(Long.toString(from), to);
  }

  /**
   * Build a <code>[from TO to]</code> expression.
   *
   * @param from Value from. Can't be null.
   * @param to Value to. Can't be null.
   * @return A <code>[from TO to]</code> expression.
   */
  public static SolrQueryExpression range(final String from, final String to) {
    return new SolrQueryRangeExpression(from, to);
  }

  /**
   * Creates a wildard expression. Wilcard expression might contains '*' or '?' chars.
   *
   * @param value A wilcard expression.
   * @return A new wildcard expression.
   */
  public static SolrQueryExpression wildcard(final String value) {
    notNull(value, "The value is required.");
    return SolrQueryTextExpression.wilcard(value);
  }

  /**
   * Build a phrase from the given value. A phrase is a term surrounded by quotes.
   *
   * @param value A term value. Required.
   * @return A phrase expression.
   */
  public static SolrQueryExpression phrase(final String value) {
    notNull(value, "The value is required.");
    return SolrQueryTextExpression.phrase(value);
  }

  /**
   * Build a term expression from the given value.
   *
   * @param value The value. Required.
   * @return A new term expression.
   */
  public static SolrQueryExpression term(final String value) {
    return SolrQueryTextExpression.term(value);
  }

  /**
   * Build an OR expression from the given expressions.
   *
   * @param values A set of values.
   * @param <T> T type of value.
   * @return A new OR expression.
   */
  public static <T> SolrQueryExpression or(final Iterable<T> values) {
    return SolrQueryBinExpression.or(convert(values));
  }

  /**
   * Build an OR expression from the given values.
   *
   * @param values A set of values.
   * @param toString The toString function to apply to each of the given values.
   * @param <T> T type of value
   * @return A new OR expression.
   */
  public static <T> SolrQueryExpression or(final Iterable<T> values,
      final Function<T, String> toString) {
    return SolrQueryBinExpression.or(convert(values, toString));
  }

  /**
   * Build an OR expression from the given expressions.
   *
   * @param expressions An expression set.
   * @return A new OR expression.
   */
  public static SolrQueryExpression or(final SolrQueryExpression... expressions) {
    return SolrQueryBinExpression.or(expressions);
  }

  /**
   * Build a firstOf (a.k.a optional) expression from the given expressions.
   *
   * @param values A set of values.
   * @param <T> T type of value
   * @return A new firstOf (a.k.a optional) expression.
   */
  public static <T> SolrQueryExpression firstOf(final Iterable<T> values) {
    return SolrQueryBinExpression.firstOf(convert(values));
  }

  /**
   * Build a firstOf (a.k.a optional) expression from the given expressions.
   *
   * @param expressions An expression set.
   * @return A new firstOf (a.k.a optional) expression.
   */
  public static SolrQueryExpression firstOf(final SolrQueryExpression... expressions) {
    return SolrQueryBinExpression.firstOf(expressions);
  }

  /**
   * Build a firstOf (a.k.a optional) expression from the given expressions.
   *
   * @param values A set of values.
   * @param toString Apply the toString function to each element.
   * @param <T> T type of value
   * @return A new firstOf (a.k.a optional) expression.
   */
  public static <T> SolrQueryExpression firstOf(final Iterable<T> values,
      final Function<T, String> toString) {
    return SolrQueryBinExpression.firstOf(convert(values, toString));
  }

  /**
   * Build an AND expression from the given expressions.
   *
   * @param values A set of values.
   * @param <T> T type of value.
   * @return A new AND expression.
   */
  public static <T> SolrQueryExpression and(final Iterable<T> values) {
    return SolrQueryBinExpression.and(convert(values));
  }

  /**
   * Build an AND expression from the given expressions.
   *
   * @param expressions An expression set.
   * @return A new AND expression.
   */
  public static SolrQueryExpression and(final SolrQueryExpression... expressions) {
    return SolrQueryBinExpression.and(expressions);
  }

  /**
   * Build an AND expression from the given expressions.
   *
   * @param values A set of values.
   * @param toString The toString function to apply to each of the given values.
   * @param <T> T type of value.
   * @return A new AND expression.
   */
  public static <T> SolrQueryExpression and(final Iterable<T> values,
      final Function<T, String> toString) {
    return SolrQueryBinExpression.and(convert(values, toString));
  }

  /**
   * Build a NOT expression from the given expressions.
   *
   * @param expression A single expression.
   * @return A new NOT expression.
   */
  public static SolrQueryExpression not(final Object expression) {
    return SolrQueryUnaryExpression.not(convert(Arrays.asList(expression))[0]);
  }

  /**
   * Build a prohibit(-) expression from the given expressions.
   *
   * @param expression A single expression.
   * @return A new prohibit expression.
   */
  public static SolrQueryExpression prohibit(final Object expression) {
    return SolrQueryUnaryExpression.prohibit(convert(Arrays.asList(expression))[0]);
  }

  /**
   * Build a required(+) expression from the given expressions.
   *
   * @param expression A single expression.
   * @return A new required expression.
   */
  public static SolrQueryExpression required(final Object expression) {
    return SolrQueryUnaryExpression.required(convert(Arrays.asList(expression))[0]);
  }

  /**
   * Build a eq expression from the given expressions.
   *
   * @param field A field's name.
   * @param expression A single expression.
   * @return A new NOT expression.
   */
  public static SolrQueryExpression eq(final String field, final Object expression) {
    return new SolrQueryEqExpression(field, convert(Arrays.asList(expression))[0]);
  }

  /**
   * Convert expression to solr expressions.
   *
   * @param expressions An expression set.
   * @return An expressions list.
   */
  private static SolrQueryExpression[] convert(final Iterable<?> expressions) {
    return convert(expressions, new Function<Object, String>() {
      @Override
      public String apply(final Object expression) {
        return expression.toString();
      }
    });
  }

  /**
   * Convert expression to solr expressions.
   *
   * @param expressions An expression set.
   * @param toString A toString function.
   * @return An expressions list.
   */
  @SuppressWarnings({"rawtypes", "unchecked" })
  private static SolrQueryExpression[] convert(final Iterable<?> expressions,
      final Function toString) {
    List<SolrQueryExpression> expressionList = new ArrayList<SolrQueryExpression>();
    for (Object expression : expressions) {
      expressionList.add(expression instanceof SolrQueryExpression
          ? (SolrQueryExpression) expression
          : term((String) toString.apply(expression))
          );
    }
    return expressionList.toArray(new SolrQueryExpression[expressionList.size()]);
  }
}
