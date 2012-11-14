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
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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

    assertEquals(dataSource, new JpaModule().jpaDataSource(env));

    verify(env);
    PowerMock.verify(DataSources.class);
  }

  @Test
  public void entityManager() throws Exception {
    assertNotNull(new JpaModule().jpaEntityManager());
  }

  @Test
  public void entityManagerFactoryMemDB() throws Exception {
    DataSource dataSource = createMock(DataSource.class);

    final Environment env = createMock(Environment.class);
    String mode = "create";

    expect(env.getRequiredProperty(DataSources.DATABASE)).andReturn("mem");
    expect(env.getProperty(JpaModule.DB_SCHEMA, "update")).andReturn(mode);

    ReflectionUtils.doWithFields(AvailableSettings.class, new FieldCallback() {
      @Override
      public void doWith(final Field field) throws IllegalArgumentException,
          IllegalAccessException {
        String propertyName = (String) field.get(null);
        expect(env.getProperty(propertyName)).andReturn(null);
      }
    });

    PowerMock.mockStatic(DataSources.class);
    expect(DataSources.build(env)).andReturn(dataSource);

    PowerMock.replay(DataSources.class);
    replay(env);

    LocalContainerEntityManagerFactoryBean factory = new JpaModule()
        .jpaEntityManagerFactory(env,
            new String[]{JpaModuleTest.class.getPackage().getName() });

    assertEquals(H2Dialect.class.getName(),
        factory.getJpaPropertyMap().get("hibernate.dialect"));

    verify(env);
    PowerMock.verify(DataSources.class);
  }

  @Test
  public void entityManagerFactoryDB() throws Exception {
    DataSource dataSource = createMock(DataSource.class);

    final Environment env = createMock(Environment.class);
    String mode = "create";

    expect(env.getRequiredProperty(DataSources.DATABASE)).andReturn("jdbc:mysql://local");
    expect(env.getProperty(JpaModule.DB_SCHEMA, "update")).andReturn(mode);

    ReflectionUtils.doWithFields(AvailableSettings.class, new FieldCallback() {
      @Override
      public void doWith(final Field field) throws IllegalArgumentException,
          IllegalAccessException {
        String propertyName = (String) field.get(null);
        expect(env.getProperty(propertyName)).andReturn(null);
      }
    });

    PowerMock.mockStatic(DataSources.class);
    expect(DataSources.build(env)).andReturn(dataSource);

    PowerMock.replay(DataSources.class);
    replay(env);

    LocalContainerEntityManagerFactoryBean factory = new JpaModule()
        .jpaEntityManagerFactory(env,
            new String[]{JpaModuleTest.class.getPackage().getName() });

    assertEquals(MySQL5InnoDBDialect.class.getName(),
        factory.getJpaPropertyMap().get("hibernate.dialect"));

    verify(env);
    PowerMock.verify(DataSources.class);
  }

  @Test
  public void transactionManager() throws Exception {
    EntityManagerFactory emf = createMock(EntityManagerFactory.class);

    replay(emf);

    JpaTransactionManager tx = new JpaModule().jpaTransactionManager(emf);
    assertNotNull(tx);
    assertEquals(emf, tx.getEntityManagerFactory());

    verify(emf);
    PowerMock.verify(DataSources.class);
  }
}
