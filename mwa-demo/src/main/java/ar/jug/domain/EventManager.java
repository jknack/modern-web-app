package ar.jug.domain;

import static ar.jug.domain.QEvent.event;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * List, create, update and delete {@link Event}.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Controller
@Transactional
public class EventManager {

  /**
   * The {@link EntityManager} service. Required.
   */
  private EntityManager em;

  /**
   * Creates a new {@link EventManager}.
   *
   * @param em The {@link EntityManager service}. Required.
   */
  @Inject
  public EventManager(@Nonnull final EntityManager em) {
    this.em = checkNotNull(em, "The entity manager is required.");
  }

  /**
   * Default constructor. Required by Spring.
   */
  protected EventManager() {
  }

  /**
   * List all the events.
   *
   * @param criteria The event criteria.
   * @return All the events.
   */
  @RequestMapping(value = "/events", method = GET)
  @ResponseBody
  public Iterable<Event> list(@Valid final EventCriteria criteria) {
    return criteria.execute(em);
  }

  /**
   * Get the event using the id.
   *
   * @param id The event's id. Required.
   * @return The event.
   */
  @RequestMapping(value = "/events/{id}", method = GET)
  @ResponseBody
  public Event get(@PathVariable @NotEmpty final String id) {
    Event result = new JPAQuery(em)
        .from(event)
        .where(event.id.equalsIgnoreCase(id))
        .uniqueResult(event);
    if (result == null) {
      throw new NoResultException("Not found: \"" + id + "\"");
    }
    return result;
  }

  /**
   * Delete the event using the id.
   *
   * @param id The event's id. Required.
   */
  @RequestMapping(value = "/events/{id}", method = DELETE)
  @ResponseStatus(value = HttpStatus.OK)
  public void delete(@PathVariable @NotEmpty final String id) {
    em.remove(get(id));
  }

  /**
   * Creates/Updates a new event.
   *
   * @param event The event to be created or updated.
   * @return The same event.
   */
  @RequestMapping(value = "/events", method = {POST, PUT })
  @ResponseBody
  public Event event(@RequestBody @Valid final Event event) {
    return em.merge(event);
  }
}
