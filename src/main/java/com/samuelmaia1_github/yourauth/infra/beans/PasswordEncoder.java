package com.samuelmaia1_github.yourauth.infra.beans;

import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder implements IPasswordEncoder {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public Boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
