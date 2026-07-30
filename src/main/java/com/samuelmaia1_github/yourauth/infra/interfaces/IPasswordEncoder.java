package com.samuelmaia1_github.yourauth.infra.interfaces;

public interface IPasswordEncoder {
    String encode(String raw);

    Boolean matches(String raw, String hash);
}
