package com.github.jknack.mwa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebDefaultsTest {

  public static class Person {
    private String firstName;

    private String lastName;

    public Person(final String firstName, final String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    protected Person() {
    }

    public String firstName() {
      return firstName;
    }

    public String lastName() {
      return lastName;
    }

    public String getFullName() {
      return firstName + " " + lastName;
    }
  }

  @Test
  public void objectMapper() throws IOException {
    ObjectMapper objectMapper = new WebDefaults().jackson2ObjectMapper();
    Person person = new Person("John", "Doe");
    String json = objectMapper.writeValueAsString(person);
    assertEquals("{\"firstName\":\"John\",\"lastName\":\"Doe\"}", json);

    Person newPerson = objectMapper.readValue(json, Person.class);
    assertEquals("John", newPerson.firstName());
    assertEquals("Doe", newPerson.lastName());
  }
}
