package ar.jug.application;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ar.jug.domain.Event;

@Controller
@Transactional
public class EventManager {
  private EntityManager em;

  @Inject
  public EventManager(final EntityManager em) {
    this.em = em;
  }

  protected EventManager() {
  }

  @RequestMapping(value="/events", method = GET)
  @ResponseBody
  public List<Event> list() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Event> query = cb.createQuery(Event.class);
    return em.createQuery(query.select(query.from(Event.class)))
        .getResultList();
  }

  @RequestMapping(value="/events",method = POST)
  @ResponseBody
  public Event newEvent(final String name) {
    return em.merge(new Event(name));
  }
}
