package ar.jug.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.code.morphia.annotations.Embedded;
import com.google.common.collect.Lists;

@Entity
@com.google.code.morphia.annotations.Entity
public class Event {

  @Id
  @com.google.code.morphia.annotations.Id
  private String name;

  @ManyToOne
  @Embedded
  private EventType eventType;

  public Event(final String name) {
    this.name = name;
  }

  protected Event() {
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(final EventType eventType) {
    this.eventType = eventType;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public static List<Event> list() {
    return Lists.newArrayList(new Event("Modern Web Appplication Architecture"));
  }
}
