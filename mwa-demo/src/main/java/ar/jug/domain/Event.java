package ar.jug.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.common.collect.Lists;

@Entity
public class Event {

  @Id
  @GeneratedValue
  private long id;

  private String name;

  @ManyToOne
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
  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
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
