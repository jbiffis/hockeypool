package com.playoffpool.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String password = body.get("password");
        if (adminPassword.equals(password)) {
            session.setAttribute("admin", true);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpSession session) {
        Boolean admin = (Boolean) session.getAttribute("admin");
        if (Boolean.TRUE.equals(admin)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }
}
