package ar.jug.view;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ar.jug.application.EventManager;

@Controller
public class Views {

  /**
   * The event manager. Required.
   */
  private EventManager eventManager;

  @Inject
  public Views(final EventManager eventManager) {
    this.eventManager = eventManager;
  }

  @RequestMapping("/")
  public String hello(final Model model) {
    model.addAttribute("events", eventManager.list());
    return "hello";
  }
}
