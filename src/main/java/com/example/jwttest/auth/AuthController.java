package com.example.jwttest.auth;

import com.example.jwttest.user.UserRepository;
import com.example.jwttest.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(
            AuthenticationConfiguration configuration,
            JwtTokenUtil jwtTokenUtil,
            UserRepository userRepository,
            UserService userService
    ) throws Exception {
        this.authenticationManager = configuration.getAuthenticationManager();
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            com.example.jwttest.user.User saved = userService.register(request);
            return ResponseEntity.ok().body(Map.of("email", saved.getEmail(), "role", saved.getRole().name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        com.example.jwttest.user.User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));
        String token = jwtTokenUtil
                .generateToken(new User(
                        user.getEmail(),
                        request.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))), user.getPoint()
                );

        return ResponseEntity.ok(Map.of("token", token));
    }
}
