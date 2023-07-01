package com.example.enigma_rest.refreshtoken;

import com.example.enigma_rest.auth.AuthenticationResponse;
import com.example.enigma_rest.config.JwtService;
import com.example.enigma_rest.config.TokenProperties;
import com.example.enigma_rest.user.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/refresh-token")
@Slf4j
public record RefreshTokenController(
        RefreshTokenService refreshTokenService,
        JwtService jwtService,
        TokenProperties tokenProperties
) {
    @GetMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletResponse response, @CookieValue(name = "jwt") String jwt)
            throws IOException {
        if (jwt == null) {
            log.error("Refresh token is missing");
            return null;
        }
        log.info("Refresh token " + jwt);

        RefreshToken refreshToken = refreshTokenService.findByToken(jwt).orElse(null);
        if (refreshToken == null) {
            log.error("User not found");
            return null;
        }

        try {
            refreshTokenService.verifyExpiration(refreshToken);
            User user = refreshToken.getUser();
            String accessToken = jwtService.generateToken(user);

            AuthenticationResponse authenticationResponse = new AuthenticationResponse(accessToken, user.getRole());
            return ResponseEntity.ok().body(authenticationResponse);
        } catch (TokenRefreshException e) {
            response.getWriter().write("Refresh token expired!");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response, @CookieValue(name = "jwt") String jwt) {
        log.info(jwt);
        RefreshToken refreshToken = refreshTokenService.findByToken(jwt).orElse(null);
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Token not found");
        }

        refreshTokenService.deleteRefreshToken(refreshToken);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from(tokenProperties.getRefreshTokenCookieName(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }
}
