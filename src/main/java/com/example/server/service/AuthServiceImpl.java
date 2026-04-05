package com.example.server.service;

import com.example.server.dto.DangNhapDTO;
import com.example.server.model.NguoiDung;
import com.example.server.repository.NguoiDungRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service xử lý nghiệp vụ đăng nhập.
 * Kiểm tra trạng thái tài khoản trước khi xác thực.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(NguoiDungRepository nguoiDungRepository,
                          AuthenticationManager authenticationManager) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String dangNhap(DangNhapDTO dangNhapDTO) throws AuthenticationException {
        NguoiDung nguoiDung = nguoiDungRepository.findByThuDienTu(dangNhapDTO.getEmail())
                .orElseThrow(() -> new AuthenticationException("Tài khoản không tồn tại") {
                });

        if (!nguoiDung.getTrangThaiHoatDong()) {
            throw new AuthenticationException("Tài khoản đã bị vô hiệu hóa") {
            };
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dangNhapDTO.getEmail(),
                        dangNhapDTO.getMatKhau()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "Login successful";
    }
}
