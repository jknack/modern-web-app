package com.github.jknack.mwa.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Sprint {

  @Id
  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
