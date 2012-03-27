package org.knowhow.mwa.view;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.junit.Test;

/**
 * Unit test for {@link HtmlTemplates}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class HtmlTemplatesTest {

  @Test
  @SuppressWarnings("rawtypes")
  public void load() throws IOException {
    ServletContext context = createMock(ServletContext.class);
    expect(context.getResource("/partials/")).andReturn(
        new File("src/test/resources/partials").toURI().toURL())
        .anyTimes();

    Map<String, Object> model = new HashMap<String, Object>();

    replay(context);

    HtmlTemplates templates = new HtmlTemplates("/partials", "html");

    templates.init(context);
    templates.contribute("demo", model);

    assertNotNull(model.get("partials"));
    assertTrue(model.get("partials") instanceof Map);

    Map partials = (Map) model.get("partials");
    assertEquals("Template text for a.html", partials.get("a"));

    assertTrue(partials.get("sub") instanceof Map);
    Map sub = (Map) partials.get("sub");
    assertEquals("Template text for b.html", sub.get("b"));

    verify(context);
  }

}
