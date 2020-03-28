package com.redroundrobin.thirema.apirest.config;

import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
    basePackages = "com.redroundrobin.thirema.apirest.repository.postgres",
    entityManagerFactoryRef = "postgresEntityManager",
    transactionManagerRef = "postgresTransactionManager"
)
public class PostgresConfig {
  @Autowired
  private Environment env;

  /**
   * FactoryBean that creates a JPA EntityManagerFactory for the postgres DB defined in application
   * properties.
   *
   * @return the entityManager for the postgres DB
   */
  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean postgresEntityManager() {
    LocalContainerEntityManagerFactoryBean em
        = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(postgresDataSource());
    em.setPackagesToScan("com.redroundrobin.thirema.apirest.models.postgres");

    HibernateJpaVendorAdapter vendorAdapter
        = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    HashMap<String, Object> properties = new HashMap<>();
    properties.put("hibernate.hbm2ddl.auto",
        env.getProperty("hibernate.hbm2ddl.auto"));
    properties.put("hibernate.dialect",
        env.getProperty("hibernate.dialect"));
    em.setJpaPropertyMap(properties);

    return em;
  }

  /**
   * Return the dataSource that rapresent a standard Connection object for postgres database
   * defined in application.properties with the prefix "spring.postgres".
   *
   * @return the dataSource configuration for postgres database
   */
  @Primary
  @Bean
  public DataSource postgresDataSource() {

    DriverManagerDataSource dataSource
        = new DriverManagerDataSource();
    dataSource.setDriverClassName(
        env.getProperty("spring.postgres.driverClassName"));
    dataSource.setUrl(env.getProperty("spring.postgres.url"));
    dataSource.setUsername(env.getProperty("spring.postgres.username"));
    dataSource.setPassword(env.getProperty("spring.postgres.password"));

    return dataSource;
  }

  /**
   * Return the PlatformTransactionManager for the postgres db connection.
   *
   * @return the PlatformTransactionManager for the postgres db connection
   */
  @Primary
  @Bean
  public PlatformTransactionManager postgresTransactionManager() {

    JpaTransactionManager transactionManager
        = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(
        postgresEntityManager().getObject());
    return transactionManager;
  }
}