package com.redroundrobin.thirema.apirest.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

  @Value("${security.signing-key}")
  private String signingKey;

  @Value("${security.encoding-strength}")
  private String encoding_strength;

  @Value("${security.token-expiration}")
  private int token_expiration;

  @Value("${security.tfa-token-expiration}")
  private int tfa_token_expiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractType(String token) {
    return extractAllClaims(token).get("type", java.lang.String.class);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public boolean isTfa(String token) {
    return extractAllClaims(token).containsKey("tfa");
  }

  public int extractAuthCode(String token) {
    if( extractAllClaims(token).containsKey("auth_code") ) {
      return extractAllClaims(token).get("auth_code", Integer.class);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(String type, UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", type);
    return createToken(claims, userDetails.getUsername());
  }

  public String generateTfaToken(String type, int sixDigitsCode, UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", type);
    claims.put("tfa", true);
    claims.put("auth_code", sixDigitsCode);
    return createToken(claims, userDetails.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    int expiration = token_expiration;
    if( claims.containsKey("tfa") && (boolean)claims.get("tfa") ) {
      expiration = tfa_token_expiration;
    }

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + (expiration * 1000)))
        .signWith(SignatureAlgorithm.forName("HS" + encoding_strength), signingKey).compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }
}
