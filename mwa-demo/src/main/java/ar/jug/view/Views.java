package ar.jug.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The views processor.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Controller
public class Views {

  /**
   * Serve the home page.
   *
   * @return The name of the home page.
   */
  @RequestMapping("/")
  public String hello() {
    return "hello";
  }
}
