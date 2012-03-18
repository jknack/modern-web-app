package ar.jug;

import org.knowhow.mwa.Startup;
import org.knowhow.mwa.mongo.MorphiaModule;

import ar.jug.application.AppModule;
import ar.jug.domain.DomainModule;
import ar.jug.view.ViewModule;

public class Main extends Startup {

  @Override
  protected Class<?>[] modules() {
    return new Class<?>[] {MorphiaModule.class,
        DomainModule.class, AppModule.class, ViewModule.class };
  }

}
