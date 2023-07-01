package com.example.enigma_rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "token")
@Getter
@Setter
public class TokenProperties {
    private int refreshTokenExpirationMs;
    private int accessTokenExpirationMs;
    private String refreshTokenCookieName;
    private String secretKey;
}
