package com.example.server.security;

import com.example.server.model.NguoiDung;
import com.example.server.model.enums.VaiTro;
import com.example.server.repository.NguoiDungRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Load user từ database bằng email (thu_dien_tu).
 * Spring Security gọi khi xác thực và khi kiểm tra quyền.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    public CustomUserDetailsService(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungRepository.findByThuDienTu(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + email));

        // Chặn SINH_VIEN khỏi luồng đăng nhập cán bộ
        if (nguoiDung.getVaiTro() == VaiTro.SINH_VIEN) {
            throw new UsernameNotFoundException("Tài khoản không được phép truy cập trang này.");
        }

        return new CustomUserDetails(nguoiDung);
    }
}
