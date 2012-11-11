package com.globant.todomvc.domain;

import static org.apache.commons.lang3.Validate.notNull;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Transactional
@RequestMapping("/api/todos")
public class TodoManager {

  private EntityManager em;

  @Inject
  public TodoManager(final EntityManager em) {
    this.em = notNull(em, "The entity manager is requred");
  }

  protected TodoManager() {
  }

  @RequestMapping(method = GET)
  @ResponseBody
  public Iterable<Todo> list() {
    TypedQuery<Todo> query = em.createQuery("from Todo", Todo.class);
    List<Todo> todos = query.getResultList();
    return todos;
  }

  @RequestMapping(value = "/{id}", method = GET)
  @ResponseBody
  public Todo get(@PathVariable final Integer id) {
    return em.find(Todo.class, id);
  }

  @RequestMapping(method = POST)
  @ResponseBody
  public Todo create(@RequestBody final Todo todo) {
    return em.merge(todo);
  }

  @RequestMapping(value = "/{id}", method = PUT)
  @ResponseBody
  public Todo update(@RequestBody final Todo todo) {
    return em.merge(todo);
  }

  @RequestMapping(value = "/{id}", method = DELETE)
  @ResponseBody
  public Todo delete(@PathVariable final Integer id) {
    Todo todo = get(id);
    em.remove(todo);
    return todo;
  }
}
