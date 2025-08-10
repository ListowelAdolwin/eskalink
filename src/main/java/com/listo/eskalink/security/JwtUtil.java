package com.listo.eskalink.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.verification.expiration}")
    private Long verificationExpiration;

    private static final String TYPE_CLAIM = "type";
    private static final String USER_ID_CLAIM = "userId";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            JwtParser parser = Jwts.parser().verifyWith(getSigningKey()).build();
            return parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is null or empty: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put(USER_ID_CLAIM, customUserDetails.getUserId().toString());
            claims.put("role", customUserDetails.getRole().name());
        }
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateVerificationToken(String email, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userId.toString());
        claims.put(TYPE_CLAIM, "verification");
        return createToken(claims, email, verificationExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long tokenExpiration) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateVerificationToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "verification".equals(claims.get(TYPE_CLAIM)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Verification token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserIdFromToken(String token) {
        try {
            String userIdString = extractClaim(token, claims -> claims.get(USER_ID_CLAIM, String.class));
            return UUID.fromString(userIdString);
        } catch (Exception e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token format");
        }
    }
}
