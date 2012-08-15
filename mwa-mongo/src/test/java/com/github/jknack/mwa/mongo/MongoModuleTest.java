package com.github.jknack.mwa.mongo;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.github.jknack.mwa.mongo.MongoModule;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * Unit test for {@link MongoModule}.
 *
 * @author edgar.espina
 * @since 0.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MongoModule.class, SimpleMongoDbFactory.class })
public class MongoModuleTest {

  @Test
  public void mongoURI() throws Exception {
    Environment env = createMock(Environment.class);
    expect(env.getRequiredProperty("db")).andReturn("mongodb://localhost/mydb");

    replay(env);

    MongoURI mongoURI = new MongoModule().mongoURI(env);
    assertNotNull(mongoURI);
    assertNotNull(mongoURI.getHosts());
    assertTrue(mongoURI.getHosts().contains("localhost"));
    assertEquals("mydb", mongoURI.getDatabase());

    verify(env);
  }

  @Test
  public void mongo() throws Exception {
    Mongo mongo = createMock(Mongo.class);

    MongoURI uri = createMock(MongoURI.class);
    expect(uri.connect()).andReturn(mongo);

    replay(uri, mongo);

    Mongo result = new MongoModule().mongo(uri);
    assertEquals(mongo, result);

    verify(uri, mongo);
  }

  @Test
  public void mongoDbFactory() throws Exception {
    String database = "mydb";
    MongoURI uri = createMock(MongoURI.class);
    expect(uri.getUsername()).andReturn(null);
    expect(uri.getPassword()).andReturn(null);
    expect(uri.getDatabase()).andReturn(database);

    Mongo mongo = createMock(Mongo.class);

    MongoDbFactory mongoDbFactory =
        PowerMock
            .createMockAndExpectNew(SimpleMongoDbFactory.class, mongo, database);

    PowerMock.replay(SimpleMongoDbFactory.class);
    replay(uri, mongo, mongoDbFactory);

    MongoDbFactory result = new MongoModule().mongoDbFactory(uri, mongo);
    assertEquals(mongoDbFactory, result);

    verify(uri, mongo, mongoDbFactory);
    PowerMock.verify(SimpleMongoDbFactory.class);
  }

  @Test
  public void mongoDbFactoryWithCredentials() throws Exception {
    String user = "user";
    char[] pass = {'p', 'a', 's', 's' };
    String database = "mydb";
    MongoURI uri = createMock(MongoURI.class);
    expect(uri.getUsername()).andReturn(user);
    expect(uri.getPassword()).andReturn(pass);
    expect(uri.getDatabase()).andReturn(database);

    Mongo mongo = createMock(Mongo.class);

    UserCredentials credentials =
        new UserCredentials(user, String.valueOf(pass));
    MongoDbFactory mongoDbFactory =
        PowerMock
            .createMockAndExpectNew(SimpleMongoDbFactory.class, mongo,
                database, credentials);

    PowerMock.replay(SimpleMongoDbFactory.class);
    replay(uri, mongo, mongoDbFactory);

    MongoDbFactory result = new MongoModule().mongoDbFactory(uri, mongo);
    assertEquals(mongoDbFactory, result);

    verify(uri, mongo, mongoDbFactory);
    PowerMock.verify(SimpleMongoDbFactory.class);
  }

  @Test
  public void exceptionTranslator() {
    assertNotNull(new MongoModule().mongoExceptionTranslator());
  }
}
