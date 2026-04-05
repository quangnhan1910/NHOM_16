package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller xử lý trang đăng nhập.
 * Chỉ dùng GET — POST /login do Spring Security formLogin xử lý.
 */
@Controller
public class DangNhapController {

    /**
     * Hiển thị trang đăng nhập.
     * error=true  → đăng nhập thất bại
     * logout=true → đăng xuất thành công
     */
    @GetMapping("/login")
    public String hienThiTrangDangNhap(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String disabled,
            Model model) {
        if (error != null) {
            model.addAttribute("loiDangNhap", "Tài khoản hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("thongBaoDangXuat", "Bạn đã đăng xuất thành công");
        }
        if (disabled != null) {
            model.addAttribute("loiDangNhap", "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
        }
        return "auth/dang-nhap";
    }
}
