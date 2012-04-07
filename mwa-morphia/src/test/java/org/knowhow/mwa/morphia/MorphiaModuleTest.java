package org.knowhow.mwa.morphia;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.common.collect.Sets;
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
    @SuppressWarnings("unchecked")
    Set<Class<?>> classes =
        Sets.newHashSet(TestEntity.class, TestEmbedded.class);

    MorphiaConfigurer configurer = createMock(MorphiaConfigurer.class);
    expect(configurer.getClasses()).andReturn(classes);

    replay(configurer);

    Morphia morphia = new MorphiaModule().morphia(configurer);
    assertNotNull(morphia);

    verify(configurer);
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
