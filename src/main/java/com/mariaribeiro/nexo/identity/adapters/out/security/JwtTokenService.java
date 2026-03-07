package com.mariaribeiro.nexo.identity.adapters.out.security;

import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.application.auth.SessionToken;
import com.mariaribeiro.nexo.identity.application.auth.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService implements TokenServicePort {

    private final Clock clock;
    private final AuthTokenProperties properties;
    private final Key signingKey;

    public JwtTokenService(Clock clock, AuthTokenProperties properties) {
        this.clock = clock;
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public SessionToken issueToken(UUID userId, String normalizedEmail) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("email", normalizedEmail)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new SessionToken(token, expiresAt);
    }

    @Override
    public TokenClaims verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .clock(() -> Date.from(Instant.now(clock)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenClaims(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.getExpiration().toInstant());
        } catch (ExpiredJwtException exception) {
            throw new InvalidTokenException("Token expired", exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Token invalid", exception);
        }
    }
}

