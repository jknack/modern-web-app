package com.github.edgarespina.mwa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Helper class for beans.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
public final class Beans {

  /**
   * Not allowed.
   */
  private Beans() {
  }

  /**
   * Look for bean of an specific type in the Application Context.
   *
   * @param context The application context.
   * @param beanType The bean type to look for.
   * @param <T> The bean generic type.
   * @return All the of the specific types found in the Application Context.
   */
  public static <T> List<T> lookFor(final ApplicationContext context,
      final Class<T> beanType) {
    Collection<T> beans = context.getBeansOfType(beanType).values();
    List<T> result = new ArrayList<T>();
    if (beans != null) {
      result.addAll(beans);
    }
    return result;
  }

  /**
   * Look for bean of an specific type in the Application Context.
   *
   * @param context The application context.
   * @param beanType The bean type to look for.
   * @param <T> The bean generic type.
   * @return The matching bean or null.
   */
  public static <T> T get(final ApplicationContext context,
      final Class<T> beanType) {
    try {
      return context.getBean(beanType);
    } catch (BeansException ex) {
      return null;
    }
  }
}
