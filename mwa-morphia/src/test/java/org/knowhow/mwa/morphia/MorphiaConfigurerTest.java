package org.knowhow.mwa.morphia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;

/**
 * Unit test for {@link MorphiaConfigurer}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MorphiaConfigurerTest {

  @Test
  public void scan() throws Exception {
    Set<Class<?>> classes = new MorphiaConfigurer(getClass().getPackage()).getClasses();
    assertNotNull(classes);
    classes.remove(TestEntity.class);
    classes.remove(TestEmbedded.class);
    assertEquals(0, classes.size());
  }
}
