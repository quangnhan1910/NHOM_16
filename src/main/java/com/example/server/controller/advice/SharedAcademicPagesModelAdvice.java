package com.example.server.controller.advice;

import com.example.server.model.enums.VaiTro;
import com.example.server.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Các trang ngân hàng câu hỏi / đề thi dùng chung cho quản trị và giảng viên.
 * Khi người dùng có vai trò GIANG_VIEN, giao diện dùng sidebar giảng viên thay vì quản trị.
 * <p>
 * Lọc theo URI (không dùng assignableTypes) để tránh trường hợp {@code @ModelAttribute}
 * không được gộp vào model khi xử lý một số controller.
 */
@ControllerAdvice
public class SharedAcademicPagesModelAdvice {

    private static final String ROLE_GIANG_VIEN = "ROLE_GIANG_VIEN";

    @ModelAttribute
    public void boSungGiaoDienGiangVien(Model model, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return;
        }
        if (!uri.startsWith("/ngan-hang-cau-hoi") && !uri.startsWith("/de-thi")) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean dungGiaoDienGiangVien = false;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails cud) {
            dungGiaoDienGiangVien = cud.getNguoiDung().getVaiTro() == VaiTro.GIANG_VIEN
                    || auth.getAuthorities().stream().anyMatch(a -> ROLE_GIANG_VIEN.equals(a.getAuthority()));
        }

        model.addAttribute("dungGiaoDienGiangVien", dungGiaoDienGiangVien);
        if (dungGiaoDienGiangVien && auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            model.addAttribute("lecturerSidebarTen", cud.getNguoiDung().getHoTen());
            model.addAttribute("lecturerSidebarEmail", cud.getNguoiDung().getThuDienTu());
        }
    }
}
