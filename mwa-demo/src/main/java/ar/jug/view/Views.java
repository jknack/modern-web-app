package ar.jug.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Views {


  @RequestMapping("/")
  public String hello(final Model model) {
    return "hello";
  }
}
