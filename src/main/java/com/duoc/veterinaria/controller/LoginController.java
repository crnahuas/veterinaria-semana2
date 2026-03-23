package com.duoc.veterinaria.controller;

import com.duoc.veterinaria.config.JwtConfig;
import com.duoc.veterinaria.model.Usuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class LoginController {

    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    public LoginController(JwtConfig jwtConfig, AuthenticationManager authenticationManager) {
        this.jwtConfig = jwtConfig;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam("user") String username,
            @RequestParam("encryptedPass") String encryptedPass) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, encryptedPass)
            );
            Usuario user = (Usuario) authentication.getPrincipal();

            String bearerToken = jwtConfig.generateBearerToken(username, user.getRole());
            Map<String, String> response = new LinkedHashMap<>();
            response.put("token", bearerToken);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid login");
        }
    }
}
