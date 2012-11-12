package com.github.jknack.mwa.jpa;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Unit test for {@link JpaModule}.
 *
 * @author edgar.espina
 * @since 0.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JpaModule.class, DataSources.class })
public class JpaModuleTest {

  @Test
  public void dataSource() throws Exception {
    DataSource dataSource = createMock(DataSource.class);

    Environment env = createMock(Environment.class);

    PowerMock.mockStatic(DataSources.class);
    expect(DataSources.build(env)).andReturn(dataSource);

    PowerMock.replay(DataSources.class);
    replay(env);

    assertEquals(dataSource, new JpaModule().dataSource(env));

    verify(env);
    PowerMock.verify(DataSources.class);
  }

  @Test
  public void entityManager() throws Exception {
    assertNotNull(new JpaModule().entityManager());
  }

  @Test
  public void entityManagerFactory() throws Exception {
    DataSource dataSource = createMock(DataSource.class);

    final Environment env = createMock(Environment.class);
    String mode = "create";
    expect(env.getProperty(JpaModule.DB_SCHEMA, "update")).andReturn(mode);

    ReflectionUtils.doWithFields(AvailableSettings.class, new FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException,
          IllegalAccessException {
        String propertyName = (String) field.get(null);
        expect(env.getProperty(propertyName)).andReturn(null);
      }
    });

    PowerMock.mockStatic(DataSources.class);
    expect(DataSources.build(env)).andReturn(dataSource);

    PowerMock.replay(DataSources.class);
    replay(env);

    new JpaModule()
        .entityManagerFactory(env,
            new String[] {JpaModuleTest.class.getPackage().getName()});

    verify(env);
    PowerMock.verify(DataSources.class);
  }

  @Test
  public void transactionManager() throws Exception {
    EntityManagerFactory emf = createMock(EntityManagerFactory.class);

    replay(emf);

    JpaTransactionManager tx = new JpaModule().transactionManager(emf);
    assertNotNull(tx);
    assertEquals(emf, tx.getEntityManagerFactory());

    verify(emf);
    PowerMock.verify(DataSources.class);
  }
}
