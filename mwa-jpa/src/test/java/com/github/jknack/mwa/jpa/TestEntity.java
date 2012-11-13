package com.github.jknack.mwa.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TestEntity {
  @Id
  public String id;
}
