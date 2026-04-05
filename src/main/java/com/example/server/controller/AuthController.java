package com.example.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API xử lý auth (đăng xuất).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Đăng xuất: hủy session và trả về JSON.
     */
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đăng xuất thành công",
            "redirectUrl", "/"
        ));
    }
}
