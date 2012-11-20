package com.github.jknack.mwa.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.jknack.mwa.ApplicationContextConfigurer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = JpaFixturesIntegrationTest.class, classes = JpaModule.class)
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class JpaFixturesIntegrationTest extends AnnotationConfigContextLoader {

  @Override
  protected void customizeContext(final GenericApplicationContext context) {
    MutablePropertySources propertySources = new MutablePropertySources();
    // use mem db and publish a namespace
    Map<String, Object> testProperties = new HashMap<String, Object>();
    testProperties.put("db", "mem");
    testProperties.put("application.ns", getClass().getPackage().getName());

    context.registerBeanDefinition("conversionService", new RootBeanDefinition(
        DefaultConversionService.class));

    propertySources.addFirst(new MapPropertySource("integrationTest", testProperties));

    ApplicationContextConfigurer.configure(context, propertySources);
  }

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
