package com.example.server.controller.advice;

import com.example.server.controller.GiamSatController;
import com.example.server.controller.LecturerController;
import com.example.server.controller.ThongKeController;
import com.example.server.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Bổ sung tên / email cho footer sidebar giảng viên trên các trang /lecturer.
 */
@ControllerAdvice(assignableTypes = {LecturerController.class, GiamSatController.class, ThongKeController.class})
public class LecturerViewModelAdvice {

    @ModelAttribute
    public void lecturerSidebarUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails cud) {
            model.addAttribute("lecturerSidebarTen", cud.getNguoiDung().getHoTen());
            model.addAttribute("lecturerSidebarEmail", cud.getNguoiDung().getThuDienTu());
        }
    }
}
