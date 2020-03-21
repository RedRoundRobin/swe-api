package com.redroundrobin.thirema.apirest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import java.util.Properties;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
    basePackages = "com.redroundrobin.thirema.apirest.repository.timescale",
    entityManagerFactoryRef = "timescaleEntityManager",
    transactionManagerRef = "timescaleTransactionManager"
)
public class TimescaleConfig {
  @Autowired
  private Environment env;

  @Bean
  public LocalContainerEntityManagerFactoryBean timescaleEntityManager() {
    LocalContainerEntityManagerFactoryBean em
        = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(timescaleDataSource());
    em.setPackagesToScan(
        new String[]{"com.redroundrobin.thirema.apirest.models.timescale"});

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

  @Bean
  public DataSource timescaleDataSource() {

    DriverManagerDataSource dataSource
        = new DriverManagerDataSource();
    dataSource.setDriverClassName(
        env.getProperty("spring.timescale.driverClassName"));
    dataSource.setUrl(env.getProperty("spring.timescale.url"));
    dataSource.setUsername(env.getProperty("spring.timescale.username"));
    dataSource.setPassword(env.getProperty("spring.timescale.password"));
    //dataSource.setConnectionProperties(new Properties().);

    return dataSource;
  }

  @Bean
  public PlatformTransactionManager timescaleTransactionManager() {

    JpaTransactionManager transactionManager
        = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(
        timescaleEntityManager().getObject());
    return transactionManager;
  }
}