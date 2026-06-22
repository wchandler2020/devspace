package com.tiltedhat.devspace.service;

import com.tiltedhat.devspace.entity.User;
import com.tiltedhat.devspace.repository.UserRepository;
import com.tiltedhat.devspace.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String registerUser(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Error: Username is already taken.");
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // hash the raw password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);
        return "New user registered successfully";
    }

    // 🔑 NEW LOGIN METHOD
    public String loginUser(LoginRequest request) {
        // This authentication manager automatically utilizes our CustomUserDetailsService under the hood
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        // Save authentication state in Spring Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate and return the secure token string
        return jwtTokenProvider.generateToken(authentication);
    }
}
