package com.redroundrobin.thirema.apirest.config;

import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.utils.JwtRequestFilter;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserService userService;

  private final JwtRequestFilter jwtRequestFilter;

  private final String[] publicRequests = new String[]{"/auth", "/auth/telegram",
      "/v3/api-docs.yaml"};

  @Autowired
  public SecurityConfig(JwtUtil jwtUtil, UserService userService) {
    this.userService = userService;
    this.jwtRequestFilter = new JwtRequestFilter(jwtUtil, userService,
        new HashSet<>(Arrays.asList(publicRequests)));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests().antMatchers(publicRequests).permitAll()
        .anyRequest().authenticated()
        .and().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  @Bean
  public CustomAuthenticationManager authenticationManagerBean() {
    return new CustomAuthenticationManager(userService);
  }
}
