package ar.jug.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.code.morphia.annotations.Embedded;
import com.google.common.collect.Lists;

@Entity
@Embedded
public class EventType {

  @Id
  private String name;

  public EventType(final String name) {
    this.name = name;
  }

  protected EventType() {
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
