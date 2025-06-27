package no.java.submit.util;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class SessionSecretHelper {

    @Inject
    @Named("app.secret")
    String appSecret;

    public String get(String sessionId) {
        var str = String.format("%s:%s", appSecret, sessionId);
        return BaseEncoding.base32().omitPadding().encode(Hashing.sha256().hashBytes(str.getBytes()).asBytes()).toLowerCase();
    }

    public void validate(String sessionId, String sessionSecret) {
        if (!get(sessionId).equals(sessionSecret)) {
            throw new NotAuthorizedException("Invalid session secret");
        }
    }
}
