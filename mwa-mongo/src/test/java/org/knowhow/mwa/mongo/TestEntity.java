package org.knowhow.mwa.mongo;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity
public class TestEntity {
  @Id
  public String id;
}
