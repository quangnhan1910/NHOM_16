package com.example.server.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý điều hướng sau khi đăng nhập thành công, dựa trên vai trò của user.
 * - QUAN_TRI_VIEN → /admin
 * - GIANG_VIEN    → /giang-vien
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String ROLE_ADMIN   = "ROLE_QUAN_TRI_VIEN";
    private static final String ROLE_LECTURER = "ROLE_GIANG_VIEN";
    private static final String ROLE_STUDENT  = "ROLE_SINH_VIEN";
    private static final String REDIRECT_ADMIN   = "/admin";
    private static final String REDIRECT_LECTURER = "/lecturer/bang-dieu-khien-giang-vien";
    private static final String REDIRECT_STUDENT  = "/sinh-vien";

    public CustomAuthenticationSuccessHandler() {
        super();
        setDefaultTargetUrl(REDIRECT_ADMIN);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = REDIRECT_ADMIN;

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (ROLE_ADMIN.equals(role)) {
                targetUrl = REDIRECT_ADMIN;
                break;
            }
            if (ROLE_LECTURER.equals(role)) {
                targetUrl = REDIRECT_LECTURER;
                break;
            }
            if (ROLE_STUDENT.equals(role)) {
                targetUrl = REDIRECT_STUDENT;
                break;
            }
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
