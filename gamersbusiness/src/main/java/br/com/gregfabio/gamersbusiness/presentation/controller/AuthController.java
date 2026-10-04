package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gregfabio.gamersbusiness.application.service.AuthService;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.LoginRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.RegisterRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.ProfileResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.TokenResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProfileResponse.from(authService.register(request.username(), request.email(), request.password())));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authService.login(request.email(), request.password()));
    }
}
