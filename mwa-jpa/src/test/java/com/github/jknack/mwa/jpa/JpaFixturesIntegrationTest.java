package com.github.jknack.mwa.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = JpaModule.class, initializers = IntegrationTestContextInitializer.class)
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class JpaFixturesIntegrationTest {
  @Inject
  private EntityManager em;

  @Test
  public void todo1() {
    Todo todo1 = em.find(Todo.class, 1);
    assertNotNull(todo1);
    assertEquals(1, todo1.getId());
    assertEquals("Maven 3.x", todo1.getTitle());
    assertNotNull(todo1.getSprint());
    assertEquals("Nov 2012", todo1.getSprint().getName());
  }

  @Test
  public void todo2() {
    Todo todo2 = em.find(Todo.class, 2);
    assertNotNull(todo2);
    assertEquals(2, todo2.getId());
    assertEquals("Servlet 3.x", todo2.getTitle());
    assertNotNull(todo2.getDependsOn());
    assertEquals("Maven 3.x", todo2.getDependsOn().getTitle());
    assertNotNull(todo2.getSprint());
    assertEquals("Dec 2012", todo2.getSprint().getName());
  }

  @Test
  public void todos() {
    TypedQuery<Todo> todos = em.createQuery("from Todo", Todo.class);
    assertNotNull(todos);
    assertNotNull(todos.getResultList());
    assertEquals(11, todos.getResultList().size());
  }

  @Test
  public void sprints() {
    TypedQuery<Sprint> sprintList = em.createQuery("from Sprint", Sprint.class);
    assertNotNull(sprintList);
    assertNotNull(sprintList.getResultList());
    assertEquals(2, sprintList.getResultList().size());
  }
}
