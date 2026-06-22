package com.tiltedhat.devspace.controller;

import com.tiltedhat.devspace.service.AuthService;
import com.tiltedhat.devspace.service.JwtAuthResponse;
import com.tiltedhat.devspace.service.LoginRequest;
import com.tiltedhat.devspace.service.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        try{
            String result = authService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.loginUser(loginRequest);
            // Wrap the raw string token in our clean response DTO object
            return ResponseEntity.ok(new JwtAuthResponse(token));
        } catch (Exception e) {
            // If authentication fails (bad password or username), return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username, email, or password.");
        }
    }
}
