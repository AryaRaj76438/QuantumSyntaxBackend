package com.quantumsyntax.controller;

import com.quantumsyntax.dto.request.LoginRequest;
import com.quantumsyntax.dto.request.RegisterRequest;
import com.quantumsyntax.dto.response.AuthResponse;
import com.quantumsyntax.dto.response.UserResponse;
import com.quantumsyntax.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        /*
         * JWT authentication is stateless.
         *
         * There is no server-side session to invalidate.
         * The client should remove the access token.
         *
         * If token revocation is required later,
         * implement a token blacklist/revocation mechanism.
         */
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                authService.getCurrentUser(
                        userDetails.getUsername()
                )
        );
    }
}