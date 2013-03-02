package com.github.jknack.mwa.solr;

import static com.github.jknack.mwa.solr.SolrQueryBuilder.and;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.any;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.eq;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.firstOf;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.none;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.not;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.or;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.phrase;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.prohibit;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.range;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.required;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.ternary;
import static com.github.jknack.mwa.solr.SolrQueryBuilder.wildcard;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class SolrQueryBuilderTest {

  @Test
  public void eqExpression() {
    assertEquals("field:Hello", eq("field", "Hello").queryString());
  }

  @Test
  public void rangeExpression() throws ParseException {
    assertEquals("[2013-02-17T00:00:00.000Z TO 2013-02-18T00:00:00.000Z]",
        range(date("02/17/2013"), date("02/18/2013")).queryString());

    assertEquals("[2013-02-17T00:00:00.000Z TO NOW]",
        range(date("02/17/2013"), "NOW").queryString());

    assertEquals("[* TO 2013-02-18T00:00:00.000Z]", range("*", date("02/18/2013")).queryString());

    assertEquals("[B TO C]", range("B", "C").queryString());

    assertEquals("[300000 TO *]", range(300000, "*").queryString());

    assertEquals("[* TO 300000]", range("*", 300000).queryString());

    assertEquals("f:(([B TO C] -C))", eq("f", firstOf(range("B", "C"), prohibit("C")))
        .queryString());
  }

  @Test
  public void orExpression() {
    assertEquals("(lucene OR solr)", or(Arrays.asList("lucene", "solr")).queryString());

    assertEquals("(field:lucene OR solr)",
        or(Arrays.asList(eq("field", "lucene"), "solr")).queryString());

    assertEquals("(lucene OR f:solr)", or(Arrays.asList("lucene", eq("f", "solr")))
        .queryString());

    assertEquals("(field:lucene OR f:solr)",
        or(Arrays.asList(eq("field", "lucene"), eq("f", "solr"))).queryString());
  }

  @Test
  public void andExpression() {
    assertEquals("(lucene AND solr)", and(Arrays.asList("lucene", "solr")).queryString());

    assertEquals("(field:lucene AND solr)",
        and(Arrays.asList(eq("field", "lucene"), "solr")).queryString());

    assertEquals("(lucene AND f:solr)", and(Arrays.asList("lucene", eq("f", "solr")))
        .queryString());

    assertEquals("(field:lucene AND f:solr)",
        and(Arrays.asList(eq("field", "lucene"), eq("f", "solr"))).queryString());
  }

  @Test
  public void notExpression() {
    assertEquals("(lucene NOT solr)", firstOf(Arrays.asList("lucene", not("solr")))
        .queryString());
  }

  @Test
  public void requiredExpression() {
    assertEquals("(+lucene +solr)", firstOf(required("lucene"), required("solr")).queryString());
  }

  @Test
  public void prohibitExpression() {
    assertEquals("(lucene -(field:*))",
        firstOf(Arrays.asList("lucene", prohibit(eq("field", any())))).queryString());
  }

  @Test
  public void ternaryExpression() {
    assertEquals("f1:v1", ternary(true, eq("f1", "v1"), none()).queryString());
    assertEquals("", ternary(false, eq("f1", "v1"), none()).queryString());
  }

  @Test
  public void noneExpr() {
    assertEquals("", none().queryString());
  }

  @Test
  public void anyExpr() {
    assertEquals("*", any().queryString());
  }

  @Test
  public void allExpr() {
    assertEquals("*:*", eq("*", "*").queryString());
  }

  @Test
  public void wildcardExpr() {
    assertEquals("hel*", wildcard("hel*").queryString());
    assertEquals("hel?", wildcard("hel?").queryString());
    assertEquals("hel*\\-", wildcard("hel*-").queryString());
  }

  @Test
  public void phraseExpr() {
    assertEquals("\"hel*\"", phrase("hel*").queryString());

    assertEquals("\"hello world\"", phrase("hello world").queryString());
  }

  @Test
  public void complexExpression() {
    String query =
        and(
            eq("type", or(Arrays.asList("t1", "t2", "t3"))),
            or(
                eq("f1", "v1"),
                eq("f2", "v2"),
                // true? expr : none
                ternary(true,
                    and(
                        eq("c3", "v3"),
                        prohibit(eq("c4", any()))
                    ),
                    none()
                )
            )
        ).queryString();
    assertEquals(
        "(type:((t1 OR t2 OR t3)) AND (f1:v1 OR f2:v2 OR (c3:v3 AND -(c4:*))))",
        query);
  }

  Date date(final String date) throws ParseException {
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));

    return df.parse(date);
  }
}
