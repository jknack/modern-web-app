package org.knowhow.mwa.wro4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.knowhow.mwa.view.AbstractModelContribution;
import org.knowhow.mwa.view.ModelContribution;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.InMemoryCacheableWroModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;

/**
 * Base class for {@link ModelContribution} based on Wro4j.
 *
 * @author edgar.espina
 * @since 0.1
 */
public abstract class WroContribution extends AbstractModelContribution {

  /**
   * The {@link WroModel} factory.
   */
  private WroModelFactory factory;

  /**
   * A clear cache flag.
   */
  protected final String version = new SimpleDateFormat("yyyyMMdd.hhmmss")
      .format(new Date());

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(final ServletContext context) throws IOException {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        // TODO: make this configurable.
        return context.getResourceAsStream("/WEB-INF/wro.xml");
      }
    };
    if (useCache()) {
      factory = new InMemoryCacheableWroModelFactory(factory);
    }
  }

  /**
   * The {@link WroModel} from wro.xml.
   *
   * @param candidate The group candidate name.
   * @return The {@link WroModel} from wro.xml.
   */
  protected Group lookupGroup(final String candidate) {
    Set<String> names = new LinkedHashSet<String>();
    names.add(candidate);
    names.add(candidate.replace("-", "."));
    names.add("default");
    WroModel model = factory.create();
    for (String name : names) {
      try {
        return model.getGroupByName(name);
      } catch (InvalidGroupNameException ex) {
        // It's ok, just go on.
        logger.trace("Group not found: ", ex);
      }
    }
    throw new InvalidGroupNameException("Group(s) not found: " + names);
  }
}
