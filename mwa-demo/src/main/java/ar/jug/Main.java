package ar.jug;

import org.knowhow.mwa.Startup;
import org.knowhow.mwa.jpa.JpaModule;

import ar.jug.application.AppModule;
import ar.jug.domain.DomainModule;
import ar.jug.view.ViewModule;

public class Main extends Startup {

  @Override
  protected Class<?>[] modules() {
    return new Class<?>[] {JpaModule.class, DomainModule.class,
        AppModule.class, ViewModule.class };
  }

  @Override
  protected String dispatcherMapping() {
    return "/*";
  }
}
