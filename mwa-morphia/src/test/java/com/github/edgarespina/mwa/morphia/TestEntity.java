package com.github.edgarespina.mwa.morphia;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity
public class TestEntity {
  @Id
  public String id;
}
