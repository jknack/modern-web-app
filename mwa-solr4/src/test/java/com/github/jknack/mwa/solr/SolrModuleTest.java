package com.github.jknack.mwa.solr;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrServer;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SolrModuleTest {

  @Test
  public void bootSolr() throws InterruptedException {
    System.setProperty("solr.home", "/solr-home");
    // Don't conflict with Solr 3.x module during maven testing
    System.setProperty("application.name", "solr4x");

    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(SolrModule.class);

    SolrServer server1 = context.getBean("core1", SolrServer.class);
    assertNotNull(server1);

    SolrServer server2 = context.getBean("core2", SolrServer.class);
    assertNotNull(server2);

    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
    context.destroy();
    context.close();
  }
}
