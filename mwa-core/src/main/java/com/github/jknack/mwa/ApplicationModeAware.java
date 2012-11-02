package com.github.jknack.mwa;

import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any bean that wishes to be notified
 * of the {@link ApplicationMode} that it runs in.
 *
 * @author edgar.espina
 * @since 0.2.3
 */
public interface ApplicationModeAware extends Aware {

  /**
   * Set the {@code Mode} that this object runs in.
   * @param mode The mode runs in.
   */
  void setMode(ApplicationMode mode);
}
