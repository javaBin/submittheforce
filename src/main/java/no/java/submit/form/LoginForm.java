package no.java.submit.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class LoginForm {

    @NotNull
    @Email
    public String email;

}
