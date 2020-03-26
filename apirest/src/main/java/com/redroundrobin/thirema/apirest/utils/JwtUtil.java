package com.redroundrobin.thirema.apirest.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtUtil {

  @Value("${security.signing-key}")
  private String signingKey;

  @Value("${security.encoding-strength}")
  private String encodingStrength;

  @Value("${security.token-expiration}")
  private int tokenExpiration;

  @Value("${security.tfa-token-expiration}")
  private int tfaTokenExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractType(String token) {
    return extractAllClaims(token).get("type", java.lang.String.class);
  }

  public int extractRole(String token) {
    if (extractAllClaims(token).containsKey("role")) {
      return extractAllClaims(token).get("role", Integer.class);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public boolean isTfa(String token) {
    return extractAllClaims(token).containsKey("tfa");
  }

  /**
   * Method that return the authCode decoded from token if found, else it throw
   * IllegalArgumentException.
   *
   * @param token of type "tfa" that would contain the auth code encoded
   * @return the auth code decoded from the token
   */
  public String extractAuthCode(String token) {
    if (extractAllClaims(token).containsKey("auth_code")) {
      return extractAllClaims(token).get("auth_code", String.class);
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

  /**
   * Method that return a jwt token generated with the @type and the @userDetails.
   *
   * @param type type of the token to be generated ("webapp" | "tfa" | "telegram" supported)
   * @param userDetails userDetails class that contain username and password to be encoded in token
   * @return jwt token that will be generated
   */
  public String generateToken(String type, UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", type);
    claims.put("role",
        String.valueOf(userDetails.getAuthorities().stream().findFirst().toString()));
    return createToken(claims, userDetails.getUsername());
  }

  /**
   * Method that return a jwt token generated with the @type, the @expiration and the @userDetails.
   *
   * @param type type of the token to be generated ("webapp" | "tfa" | "telegram" supported)
   * @param expiration time set for the expiration
   * @param userDetails userDetails class that contain username and password to be encoded in token
   * @return jwt token that will be generated
   */
  public String generateTokenWithExpiration(String type, Date expiration,UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", type);
    claims.put("role",
        String.valueOf(userDetails.getAuthorities().stream().findFirst().toString()));
    return createTokenWithExpiration(claims, expiration, userDetails.getUsername());
  }

  /**
   * Method that return a jwt token generated with the @type, the @authCode and @userDetails.
   *
   * @param type type of the token to be generated, usually "tfa" for this method
   *             ("webapp" | "tfa" | "telegram" supported)
   * @param authCode two factor authentication code that will be encoded in token
   * @param userDetails userDetails class that contain username and password to be encoded in token
   * @return jwt token that will be generated
   */
  public String generateTfaToken(String type, String authCode, UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", type);
    claims.put("tfa", true);
    claims.put("auth_code", authCode);
    return createToken(claims, userDetails.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    int expiration = tokenExpiration;
    if (claims.containsKey("tfa") && (boolean)claims.get("tfa")) {
      expiration = tfaTokenExpiration;
    }

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + (expiration * 1000)))
        .signWith(SignatureAlgorithm.forName("HS" + encodingStrength), signingKey).compact();
  }

  private String createTokenWithExpiration(Map<String, Object> claims, Date expiration,
                                           String subject) {

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(expiration)
        .signWith(SignatureAlgorithm.forName("HS" + encodingStrength), signingKey).compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }
}
