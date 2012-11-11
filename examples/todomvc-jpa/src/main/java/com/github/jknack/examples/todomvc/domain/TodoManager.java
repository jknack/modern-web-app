package com.github.jknack.examples.todomvc.domain;

import static org.apache.commons.lang3.Validate.notNull;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A todo manager work with {@link Todo items}.
 *
 * @author edgar.espina
 *
 */
@Controller
@Transactional
@RequestMapping("/api/todos")
public class TodoManager {

  /**
   * The {@link EntityManager} em.
   */
  private EntityManager em;

  /**
   * Creates a new {@link TodoManager}.
   *
   * @param em The entity's manager. Required.
   */
  @Inject
  public TodoManager(final EntityManager em) {
    this.em = notNull(em, "The entity manager is requred");
  }

  /**
   * Required by Spring.
   */
  protected TodoManager() {
  }

  /**
   * List all the todo items.
   *
   * @return List all the todo items.
   */
  @RequestMapping(method = GET)
  @ResponseBody
  public Iterable<Todo> list() {
    TypedQuery<Todo> query = em.createQuery("from Todo", Todo.class);
    List<Todo> todos = query.getResultList();
    return todos;
  }

  /**
   * Get a todo item by id.
   *
   * @param id The todo's id.
   * @return A todo item.
   */
  @RequestMapping(value = "/{id}", method = GET)
  @ResponseBody
  public Todo get(@PathVariable final Integer id) {
    return em.find(Todo.class, id);
  }

  /**
   * Creates a new todo item.
   *
   * @param todo The todo item.
   * @return A todo item.
   */
  @RequestMapping(method = POST)
  @ResponseBody
  public Todo create(@RequestBody final Todo todo) {
    return em.merge(todo);
  }

  /**
   * Updates a new todo item.
   *
   * @param todo The todo item.
   * @return A todo item.
   */
  @RequestMapping(value = "/{id}", method = PUT)
  @ResponseBody
  public Todo update(@RequestBody final Todo todo) {
    return em.merge(todo);
  }

  /**
   * Delete a todo item by id.
   *
   * @param id The todo item id.
   */
  @RequestMapping(value = "/{id}", method = DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable final Integer id) {
    Todo todo = get(id);
    em.remove(todo);
  }
}
