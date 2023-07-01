package com.example.enigma_rest.auth;

import com.example.enigma_rest.config.JwtService;
import com.example.enigma_rest.refreshtoken.RefreshTokenProvider;
import com.example.enigma_rest.refreshtoken.RefreshTokenService;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenProvider refreshTokenProvider;

    public AuthenticationResponse register(RegisterRequest request,
                                           HttpServletResponse response) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .devices(new ArrayList<>())
                .build();
        repository.save(user);

        sendRefreshToken(user, response);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request,
                                               HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        sendRefreshToken(user, response);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .build();
    }

    private void sendRefreshToken(User user, HttpServletResponse response) {
        refreshTokenService.deleteByUserUsername(user.getUsername());
        ResponseCookie springCookie = refreshTokenProvider.createRefreshTokenCookie(user);
        response.setHeader(HttpHeaders.SET_COOKIE, springCookie.toString());
    }
}
