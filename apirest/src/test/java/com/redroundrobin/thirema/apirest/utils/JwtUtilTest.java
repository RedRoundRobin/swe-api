package com.redroundrobin.thirema.apirest.utils;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class JwtUtilTest {

  private JwtUtil jwtUtil;

  String authToken;

  String tfaToken;

  User user;

  org.springframework.security.core.userdetails.User userDetails;

  @Before
  public void setUp() {
    jwtUtil = new JwtUtil("512", 60, 60, "secretKey");

    user = new User("name","surname","email","password", User.Role.ADMIN);
    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));
    userDetails = new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), grantedAuthorities);
    authToken = jwtUtil.generateToken("webapp", userDetails);
    tfaToken = jwtUtil.generateTfaToken("tfa", new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), Collections.emptyList()), "authcode");
  }



  @Test
  public void extractAuthCodeSuccessfull() {
    String authCode = jwtUtil.extractAuthCode(tfaToken);

    assertEquals("authcode", authCode);
  }

  @Test
  public void extractAuthCodeNotTfaTokenThrowIllegalArgumentException() {
    try {
      String authCode = jwtUtil.extractAuthCode(authToken);

      assertTrue(false);
    } catch (IllegalArgumentException iae) {
      assertTrue(true);
    }
  }



  @Test
  public void extractExpirationSuccessfull() {
    Date expiration = jwtUtil.extractExpiration(tfaToken);

    assertNotNull(expiration);
  }



  @Test
  public void extractRoleSuccessfull() {
    User.Role role = jwtUtil.extractRole("Bearer " + authToken);

    assertEquals(User.Role.ADMIN, role);
  }



  @Test
  public void extractTypeSuccessfull() {
    String type = jwtUtil.extractType(authToken);

    assertEquals("webapp", type);
  }



  @Test
  public void extractUsernameSuccessfull() {
    String username = jwtUtil.extractUsername(authToken);

    assertEquals(user.getEmail(), username);
  }



  @Test
  public void generateTokenWithExpirationSuccessfull() {
    String newToken = jwtUtil.generateTokenWithExpiration("type", userDetails, Date.from(Instant.now()));

    assertNotEquals("", newToken);
  }



  @Test
  public void isTfaSuccessfull() {
    assertTrue(jwtUtil.isTfa(tfaToken));
    assertFalse(jwtUtil.isTfa(authToken));
  }



  @Test
  public void validateTokenSuccessfull() {
    assertTrue(jwtUtil.validateToken(authToken, userDetails));
  }
}
