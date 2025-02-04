package no.java.submit.util;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class ExtensionCodeHelper {

    private String secret;

    private String conferenceId;

    public ExtensionCodeHelper(String secret, String conferenceId) {
        this.secret = secret;
        this.conferenceId = conferenceId;
    }

    public boolean verify(String email, String code) {
        var source = String.format("%s:%s:%s", secret, conferenceId, email);
        var expectedCode = BaseEncoding.base64().omitPadding().encode(Hashing.sha256().hashBytes(source.getBytes()).asBytes());
        System.out.println(expectedCode);
        return expectedCode.equals(code);
    }
}
