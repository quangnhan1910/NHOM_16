package com.example.server.security;

import com.example.server.model.NguoiDung;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wrapper quanh NguoiDung entity để implements UserDetails.
 * Sau khi đăng nhập, cast Principal về CustomUserDetails để lấy NguoiDung gốc.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final NguoiDung nguoiDung;

    public CustomUserDetails(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + nguoiDung.getVaiTro().name()));
    }

    @Override
    public String getPassword() {
        return nguoiDung.getMatKhauMaHoa();
    }

    @Override
    public String getUsername() {
        return nguoiDung.getThuDienTu();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(nguoiDung.getTrangThaiHoatDong());
    }
}
