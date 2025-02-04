package no.java.submit.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class TokenHelper {

    private final String issuer;
    private final String audience;
    private final Algorithm algorithm;
    private final long expirationMin;

    private final JWTVerifier verifier;

    public TokenHelper(String issuer, String audience, String secret, long expirationMin) {
        this.issuer = issuer;
        this.audience = audience;
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationMin = expirationMin;

        this.verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build();
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public String create(String email, Map<String, String> extras) {
        var builder =JWT.create()
                .withIssuer(issuer)
                .withSubject(email)
                .withAudience(audience)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(expirationMin, ChronoUnit.MINUTES));

        for (var key : extras.keySet())
            builder = builder.withClaim(key, extras.get(key));

        return builder.sign(algorithm);
    }
}
