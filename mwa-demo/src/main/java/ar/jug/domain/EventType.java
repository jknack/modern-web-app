package ar.jug.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;

/**
 * The event type.
 *
 * @author edgar.espina
 */
@Entity
public class EventType {

  /**
   * The event type's name.
   */
  @Id
  private String name;

  /**
   * Creates a new {@link EventType}.
   *
   * @param name The event type's name.
   */
  public EventType(final String name) {
    setName(name);
  }

  /**
   * Default constructor required by JPA Provider.
   */
  protected EventType() {
  }

  /**
   * The event type's name.
   *
   * @return The event type's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the event type's name.
   * @param name The event type's name. Required.
   */
  public void setName(final String name) {
    Validate.notEmpty(name, "The event type's name is required.");
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EventType) {
      EventType that = (EventType) obj;
      return Objects.equal(this.name, that.name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return name;
  }
}
