package ar.jug.domain;

import static ar.jug.domain.QEvent.event;

import javax.persistence.EntityManager;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.expr.ComparableExpression;

/**
 * An event criteria.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class EventCriteria {

  /**
   * The page index. Optional.
   */
  @Min(1)
  private Long page;

  /**
   * The page's size. Optional.
   */
  @Min(1)
  private Long pageSize;

  /**
   * Filter events where the name start with this value. Optional.
   */
  private String startsWith;

  /**
   * The order by field. Default is: date.
   */
  private String orderBy;

  /**
   * The order direction. Default is: false.
   */
  private boolean asc = false;

  /**
   * Apply filters and collect all the events.
   *
   * @param em The {@link EntityManager} required.
   * @return A filtered set of {@link Event}.
   */
  public Iterable<Event> execute(final EntityManager em) {
    Validate.notNull(em, "The entity manager is required.");
    JPAQuery query = new JPAQuery(em);

    // Apply default order or the a supplied order.
    ComparableExpression<?> orderBy = this.orderBy == null ? event.date
        : Expressions.comparablePath(Comparable.class, event, this.orderBy);
    query.from(event)
        .orderBy(asc ? orderBy.asc() : orderBy.desc());
    // Apply starts with filter if necessary.
    if (startsWith != null) {
      query.where(event.name.startsWithIgnoreCase(startsWith));
    }
    // Apply pagination options if necessary.
    if (page != null && pageSize != null) {
      long pages = query.count() / pageSize;
      query.offset((page - 1) * pageSize);
      query.limit(pageSize);
      return new SearchResult<Event>(page,
          pageSize,
          Math.max(pages, pageSize),
          query.list(event));
    } else {
      return query.list(event);
    }
  }

  /**
   * The page index.
   *
   * @return The page index.
   */
  public Long getPage() {
    return page;
  }

  /**
   * Set the page index.
   *
   * @param page The page index. Optional.
   */
  public void setPage(final Long page) {
    this.page = page;
  }

  /**
   * The page's size.
   *
   * @return The page's size.
   */
  public Long getPageSize() {
    return pageSize;
  }

  /**
   * Set the page's size.
   *
   * @param pageSize The page's size
   */
  public void setPageSize(final Long pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Filter events where the name start with this value.
   *
   * @return the startsWith The starts with expression.
   */
  public String getStartsWith() {
    return startsWith;
  }

  /**
   * Filter events where the name start with this value.
   *
   * @param startsWith The startsWith expression.
   */
  public void setStartsWith(final String startsWith) {
    this.startsWith = startsWith;
  }

  /**
   * The order by field. Default is: date.
   *
   * @return The order by field.
   */
  public String getOrderBy() {
    return orderBy;
  }

  /**
   * Set the order by field.
   *
   * @param orderBy The order by field. Required.
   */
  public void setOrderBy(final String orderBy) {
    this.orderBy = orderBy;
  }

  /**
   * The order direction.
   *
   * @return the asc The order direction.
   */
  public boolean isAsc() {
    return asc;
  }

  /**
   * Set the order direction.
   *
   * @param asc The order direction.
   */
  public void setAsc(final boolean asc) {
    this.asc = asc;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EventCriteria) {
      EventCriteria that = (EventCriteria) obj;
      return EqualsBuilder.reflectionEquals(this, that, false);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this,
        ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
