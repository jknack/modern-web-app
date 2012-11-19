package com.github.jknack.mwa.morphia;

import static com.github.jknack.mwa.ApplicationConstants.APP_NAMESPACE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.core.env.Environment;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * Unit test for {@link MorphiaModule}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public class MorphiaModuleTest {

  @Test
  public void morphia() {
    Environment env = createMock(Environment.class);
    expect(env.getProperty(APP_NAMESPACE, String[].class)).andReturn(
        new String[]{getClass().getPackage().getName() });

    replay(env);

    Morphia morphia = new MorphiaModule().morphia(env);
    assertNotNull(morphia);

    verify(env);
  }

  @Test
  public void morphiaDatastore() {
    MongoURI uri = createMock(MongoURI.class);
    expect(uri.getDatabase()).andReturn("mydb");

    Mongo mongo = createMock(Mongo.class);

    Datastore datastore = createMock(Datastore.class);

    Morphia morphia = createMock(Morphia.class);
    expect(morphia.createDatastore(mongo, "mydb")).andReturn(datastore);

    replay(morphia, mongo, uri, datastore);

    Datastore result =
        new MorphiaModule().morphiaDatastore(morphia, mongo, uri);
    assertEquals(result, datastore);

    verify(morphia, mongo, uri, datastore);
  }
}
