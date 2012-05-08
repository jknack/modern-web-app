package com.github.edgarespina.mwa.morphia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;

import com.github.edgarespina.mwa.morphia.MorphiaConfigurer;

/**
 * Unit test for {@link MorphiaConfigurer}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MorphiaConfigurerTest {

  @Test
  public void scan() throws Exception {
    Set<Class<?>> classes =
        new MorphiaConfigurer().addPackage(getClass().getPackage()).scan();
    assertNotNull(classes);
    classes.remove(TestEntity.class);
    classes.remove(TestEmbedded.class);
    assertEquals(0, classes.size());
  }
}
