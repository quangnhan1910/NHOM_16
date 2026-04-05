package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý lỗi 403 - Không có quyền truy cập.
 */
@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String trangAccessDenied() {
        return "auth/access-denied";
    }
}
