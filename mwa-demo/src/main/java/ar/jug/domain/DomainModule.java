package ar.jug.domain;

import org.knowhow.mwa.jpa.JpaConfigurer;
import org.knowhow.mwa.mongo.MorphiaConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainModule {

  @Bean
  public JpaConfigurer jpaConfigurer() throws Exception {
    Package jugPackage = DomainModule.class.getPackage();
    return new JpaConfigurer(jugPackage);
  }

  @Bean
  public MorphiaConfigurer morphiaConfigurer() throws Exception {
    Package jugPackage = DomainModule.class.getPackage();
    return new MorphiaConfigurer(jugPackage);
  }
}
