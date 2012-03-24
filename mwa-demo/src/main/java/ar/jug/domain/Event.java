package ar.jug.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;

/**
 * Store information about JUG events.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Entity
public class Event {

  /**
   * The event's id.
   */
  @Id
  private String id;

  /**
   * The event's name.
   */
  @NotEmpty
  private String name;

  /**
   * The event's type.
   */
  @ManyToOne(cascade = CascadeType.ALL)
  @NotNull
  private EventType type;

  /**
   * The event's date.
   */
  @NotNull
  private Date date;

  /**
   * The event's location.
   */
  @NotNull
  private String location;

  /**
   * The event rating.
   */
  @Min(value = 0)
  private int rating;

  /**
   * Creates a new {@link Event}.
   *
   * @param name The event's name. Required.
   */
  public Event(final String name) {
    setName(name);
  }

  /**
   * Default constructor required by JPA provider.
   */
  protected Event() {
  }

  /**
   * The event's name.
   *
   * @return The event's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the event's name.
   *
   * @param name The event's name. Required.
   */
  public void setName(final String name) {
    this.name = name;
    this.id = IdGenerator.generate(name);
  }

  /**
   * The event's id.
   *
   * @return The event's id.
   */
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Event) {
      Event that = (Event) obj;
      return Objects.equal(this.name, that.name);
    }
    return false;
  }

  /**
   * The event's date.
   *
   * @return The event's date.
   */
  public Date getDate() {
    return date;
  }

  /**
   * Set the event's date.
   *
   * @param date The event's date. Optional.
   */
  public void setDate(final Date date) {
    this.date = date;
  }

  /**
   * The event's location.
   *
   * @return The event's location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Set the event's location.
   *
   * @param location The event's location. Required.
   */
  public void setLocation(final String location) {
    this.location = location;
  }

  /**
   * The event's type.
   *
   * @return The event's type.
   */
  public EventType getType() {
    return type;
  }

  /**
   * Set the event's type.
   *
   * @param type The event's type.
   */
  public void setType(final EventType type) {
    this.type = type;
  }

  /**
   * The event's rating.
   *
   * @return The event's rating.
   */
  public int getRating() {
    return rating;
  }

  /**
   * Set the event's rating.
   *
   * @param rating The event's rating.
   */
  public void setRating(final int rating) {
    this.rating = rating;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return name;
  }
}
