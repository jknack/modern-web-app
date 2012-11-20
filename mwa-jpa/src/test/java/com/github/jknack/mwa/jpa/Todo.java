package com.github.jknack.mwa.jpa;

import static org.apache.commons.lang3.Validate.notEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

  @ManyToOne(cascade = CascadeType.ALL)
  private Sprint sprint;

  @ManyToOne
  private Todo dependsOn;

  /**
   * Creates a Todo.
   *
   * @param id The todo's id.
   */
  public Todo(final int id) {
    this.id = id;
  }

  /**
   * Default constructor. Required by JPA.
   */
  public Todo() {
  }

  /**
   * The todo's id.
   *
   * @return The todo's id.
   */
  public int getId() {
    return id;
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

  public void setSprint(final Sprint sprint) {
    this.sprint = sprint;
  }

  public Sprint getSprint() {
    return sprint;
  }

  public Todo getDependsOn() {
    return dependsOn;
  }

  public void setDependsOn(final Todo dependsOn) {
    this.dependsOn = dependsOn;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
