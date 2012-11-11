package com.github.jknack.examples.todomvc.view;

import static org.apache.commons.lang3.Validate.notNull;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.jknack.examples.todomvc.domain.TodoManager;

/**
 * A page renderer.
 *
 * @author edgar.espina
 *
 */
@Controller
public class Views {

  /**
   * The todo manager. Required.
   */
  private TodoManager manager;

  /**
   * Creates a new {@link Views}.
   *
   * @param manager The todo manager. Required.
   */
  @Inject
  public Views(final TodoManager manager) {
    this.manager = notNull(manager, "The manager is required.");
  }

  /**
   * Renderer the home or index page and add any todo item that might exists.
   *
   * @param model The view model.
   */
  @RequestMapping("/")
  public void index(final Model model) {
    model.addAttribute("todos", manager.list());
  }
}
