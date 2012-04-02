package org.knowhow.mwa.validation;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit test for {@link ValidationModule}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class ValidationModuleTest {

  @Test
  public void validationFactory() throws Exception {
    assertNotNull(new ValidationModule().localValidatorFactoryBean());
  }

  @Test
  public void jsr303ExcetionResolver() throws Exception {
    assertNotNull(new ValidationModule().jsr303ExceptionResolver());
  }
}
