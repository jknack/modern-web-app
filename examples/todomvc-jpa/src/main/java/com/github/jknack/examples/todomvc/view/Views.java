package com.globant.todomvc.view;

import static org.apache.commons.lang3.Validate.notNull;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.globant.todomvc.domain.TodoManager;

@Controller
public class Views {

  private TodoManager manager;

  @Inject
  public Views(final TodoManager manager) {
    this.manager = notNull(manager, "The manager is required.");
  }

  @RequestMapping("/")
  public String index(final Model model) {
    model.addAttribute("todos", manager.list());
    return "index";
  }
}
