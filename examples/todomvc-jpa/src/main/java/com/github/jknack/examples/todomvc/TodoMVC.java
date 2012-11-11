package com.github.jknack.examples.todomvc;

import com.github.jknack.mwa.Startup;
import com.github.jknack.mwa.jpa.JpaModule;
import com.github.jknack.mwa.wro4j.WroModule;

/**
 * The bootstrap class.
 *
 * @author edgar.espina
 */
public class TodoMVC extends Startup {

  @Override
  protected Class<?>[] imports() {
    return new Class<?>[]{
        JpaModule.class,
        WroModule.class };
  }

}
