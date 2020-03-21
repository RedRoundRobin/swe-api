package com.redroundrobin.thirema.apirest.utils;

import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String type = null;
    String jwt = null;

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      jwt = authorizationHeader.substring(7);
      type = jwtUtil.extractType(jwt);

      // Any jwtUtil method call catch ExpiredJwtException that not permit to reach "/check" that is allowed to anyone
      if( !request.getRequestURI().equals("/check") ) {
        username = jwtUtil.extractUsername(jwt);
      }
    }

    // check if request with normal token or request to "/auth/tfa" with tfa token
    // block all calls to api if no token provided and permit only "/auth/tfa" with tfa token
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null &&
        (!jwtUtil.isTfa(jwt) || request.getRequestURI().equals("/auth/tfa"))) {

      UserDetails userDetails;

      try {
        switch (type) {
          case "webapp":
          case "tfa":
            userDetails = this.userService.loadUserByEmail(username);
            break;
          case "telegram":
            userDetails = this.userService.loadUserByTelegramName(username);
            break;
          default:
            userDetails = null;
        }
      } catch(UsernameNotFoundException | UserDisabledException ue) {
        userDetails = null;
      }

      if (userDetails != null && jwtUtil.validateToken(jwt, userDetails)) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
    }
    chain.doFilter(request, response);
  }
}
