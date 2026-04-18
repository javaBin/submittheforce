package no.java.submit.util;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@ApplicationScoped
public class CodeHelper {

    @Inject
    @Named("app.secret")
    String appSecret;

    public String generate(LocalDate date, String email) {
        return String.format("%s.%s", date, signature(date, email));
    }

    public Optional<LocalDate> validate(String code, String email) {
        if (code == null || email == null || email.isBlank())
            return Optional.empty();

        var parts = code.split("\\.", 2);
        if (parts.length != 2)
            return Optional.empty();

        LocalDate date;
        try {
            date = LocalDate.parse(parts[0]);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }

        return signature(date, email).equals(parts[1]) ? Optional.of(date) : Optional.empty();
    }

    private String signature(LocalDate date, String email) {
        var str = String.format("extension:%s:%s:%s", date, email, appSecret);
        return BaseEncoding.base32().omitPadding().encode(Hashing.sha256().hashBytes(str.getBytes()).asBytes()).toLowerCase();
    }
}
