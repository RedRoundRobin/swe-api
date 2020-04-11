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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class JwtRequestFilterTest {

  MockHttpServletRequest httpRequest;
  MockHttpServletResponse httpResponse;
  MockFilterChain filterChain;

  private JwtRequestFilter jwtRequestFilter;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @Before
  public void setUp() {
    httpRequest = new MockHttpServletRequest();
    httpResponse = new MockHttpServletResponse();
    filterChain = new MockFilterChain();
    jwtRequestFilter = new JwtRequestFilter(jwtUtil, userService, Collections.emptySet());
  }

  @Test
  public void doFilterInternalRightWebappTokenSuccessfull() throws UserDisabledException {
    httpRequest.addHeader("Authorization", "Bearer prova");

    User user = new User("name","surname","email","password", User.Role.ADMIN);
    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), Collections.emptyList());

    when(jwtUtil.extractType("prova")).thenReturn("webapp");
    when(jwtUtil.extractUsername("prova")).thenReturn(user.getEmail());
    when(jwtUtil.isTfa("prova")).thenReturn(false);
    when(jwtUtil.validateToken("prova", userDetails)).thenReturn(true);

    when(userService.loadUserByEmail(user.getEmail())).thenReturn(userDetails);

    UsernamePasswordAuthenticationToken userAuth =
        new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
    userAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

    try {
      jwtRequestFilter.doFilterInternal(httpRequest, httpResponse, filterChain);

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      assertEquals(userAuth, auth);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void doFilterInternalExpiredTokenSetResponse419() {
    httpRequest.addHeader("Authorization", "Bearer prova");

    User user = new User("name","surname","email","password", User.Role.ADMIN);
    new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(), Collections.emptyList());

    when(jwtUtil.extractType("prova")).thenThrow(
        new ExpiredJwtException(new DefaultHeader(), new DefaultClaims(), ""));

    try {
      jwtRequestFilter.doFilterInternal(httpRequest, httpResponse, filterChain);

      assertEquals(419, httpResponse.getStatus());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void doFilterInternalTelegramTokenThrowTelegramChatNotFoundException() throws TelegramChatNotFoundException, UserDisabledException {
    httpRequest.addHeader("Authorization", "Bearer prova");

    User user = new User("name","surname","email","password", User.Role.ADMIN);
    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), Collections.emptyList());

    when(jwtUtil.extractType("prova")).thenReturn("telegram");
    when(jwtUtil.extractUsername("prova")).thenReturn(user.getEmail());
    when(jwtUtil.isTfa("prova")).thenReturn(false);
    when(jwtUtil.validateToken("prova", userDetails)).thenReturn(true);

    when(userService.loadUserByTelegramName(user.getEmail())).thenThrow(new TelegramChatNotFoundException());

    try {
      jwtRequestFilter.doFilterInternal(httpRequest, httpResponse, filterChain);

      assertNull(SecurityContextHolder.getContext().getAuthentication());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void doFilterInternalNotExistentTokenType() {
    httpRequest.addHeader("Authorization", "Bearer prova");

    User user = new User("name","surname","email","password", User.Role.ADMIN);

    when(jwtUtil.extractType("prova")).thenReturn("unknown");
    when(jwtUtil.extractUsername("prova")).thenReturn(user.getEmail());
    when(jwtUtil.isTfa("prova")).thenReturn(false);

    try {
      jwtRequestFilter.doFilterInternal(httpRequest, httpResponse, filterChain);

      assertNull(SecurityContextHolder.getContext().getAuthentication());
    } catch (Exception e) {
      fail();
    }
  }
}
