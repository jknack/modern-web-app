package org.knowhow.mwa.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;

/**
 * Unit test for {@link JpaConfigurer}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class JpaConfigurerTest {

  @Test
  public void scan() throws Exception {
    Set<Class<?>> classes =
        new JpaConfigurer().addPackage(getClass().getPackage()).scan();
    assertNotNull(classes);
    classes.remove(TestEntity.class);
    classes.remove(TestEmbedded.class);
    assertEquals(0, classes.size());
  }
}
