package ar.jug.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.collect.Lists;

@Entity
public class EventType {

  @Id
  @GeneratedValue
  private long id;

  private String name;

  public EventType(final String name) {
    this.name = name;
  }

  protected EventType() {
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

  public static List<EventType> list() {
    return Lists.newArrayList(new EventType("Modern Web Appplication Architecture"));
  }
}
