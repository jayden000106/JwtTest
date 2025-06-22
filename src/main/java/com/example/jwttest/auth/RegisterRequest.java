package com.example.jwttest.auth;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
}
