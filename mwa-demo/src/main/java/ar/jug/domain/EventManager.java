package ar.jug.domain;

import static ar.jug.domain.QEvent.event;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
   * @return All the events.
   */
  @RequestMapping(value = "/events", method = GET)
  @ResponseBody
  public List<Event> list() {
    JPAQuery query = new JPAQuery(em);
    return query.from(event).
        orderBy(event.date.desc())
        .list(event);
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
