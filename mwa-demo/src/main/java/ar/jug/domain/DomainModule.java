package ar.jug.domain;

import org.knowhow.mwa.jpa.JpaConfigurer;
import org.knowhow.mwa.jpa.JpaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * The domain module. It configure the persistent provider, enabled transactions
 * and rest like services.
 *
 * @author edgar.espina
 * @since 0.1
 */
@Configuration
@EnableWebMvc
@EnableTransactionManagement
public class DomainModule {

  /**
   * Publish a {@link JpaConfigurer} required by {@link JpaModule}.
   *
   * @return A jpa configurer.
   * @throws Exception
   * @see {@link JpaConfigurer}.
   * @throws Exception If the package(s) cannot be scanned.
   */
  @Bean
  public JpaConfigurer jpaConfigurer() throws Exception {
    return new JpaConfigurer(getClass().getPackage());
  }

}
