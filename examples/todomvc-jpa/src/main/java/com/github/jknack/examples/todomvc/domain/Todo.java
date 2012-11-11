package com.github.jknack.examples.todomvc.domain;

import static org.apache.commons.lang3.Validate.notEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The todo item.
 *
 * @author edgar.espina
 *
 */
@Entity
public class Todo {

  /**
   * The todo's id. Generated.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  /**
   * The todo's title.
   */
  private String title;

  /**
   * Is it complete?
   */
  private boolean completed;

  /**
   * The todo's id.
   *
   * @return The todo's id.
   */
  public int getId() {
    return id;
  }

  /**
   * Not allowed just need for Jackson.
   *
   * @param id The todo's id.
   */
  /* package */void setId(final int id) {
    this.id = id;
  }

  /**
   * The todo's title.
   *
   * @return The todo's title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the todo's title.
   *
   * @param title The todo's title. Required.
   */
  public void setTitle(final String title) {
    this.title = notEmpty(title, "The title is required.");
  }

  /**
   * True, if completed.
   *
   * @return True, if completed.
   */
  public boolean isCompleted() {
    return completed;
  }

  /**
   * Set a the complete state.
   *
   * @param completed the complete state.
   */
  public void setCompleted(final boolean completed) {
    this.completed = completed;
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Todo) {
      return title.equalsIgnoreCase(((Todo) obj).title);
    }
    return false;
  }

  @Override
  public String toString() {
    return title;
  }
}
