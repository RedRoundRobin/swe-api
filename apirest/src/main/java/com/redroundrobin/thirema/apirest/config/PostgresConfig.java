package com.redroundrobin.thirema.apirest.config;

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

import javax.sql.DataSource;
import java.util.HashMap;

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

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean postgresEntityManager() {
    LocalContainerEntityManagerFactoryBean em
        = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(postgresDataSource());
    em.setPackagesToScan(
        new String[]{"com.redroundrobin.thirema.apirest.models.postgres"});

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