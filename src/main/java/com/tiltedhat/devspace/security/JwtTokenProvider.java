package com.tiltedhat.devspace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Removed 'final' and removed the hardcoded string assignment.
    // Spring will look for an environment variable named JWT_SECRET at runtime.
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    // By adding the :604800000 fallback, it ensures that if the environment
    // variable is missing, it sets a real 7-day expiration window instead of 0.
    @Value("${JWT_EXPIRATION:604800000}")
    private long jwtExpirationInMs;

    // ⬇️ ADD THIS TEMPORARY DEBUG METHOD
    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("====== JWT SECRET CHECK ======");
        System.out.println("Loaded Secret: " + jwtSecret);
        System.out.println("==============================");
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 1. Generate the JWT Token when a user logs in successfully
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 2. Extract the username out of a given JWT Token
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 3. Validate that the token is real and hasn't expired
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty.");
        }
        return false;
    }
}