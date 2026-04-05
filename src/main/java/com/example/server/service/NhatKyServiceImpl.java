package com.example.server.service;

import com.example.server.model.NguoiDung;
import com.example.server.model.NhatKyHeThong;
import com.example.server.repository.NhatKyHeThongRepository;
import com.example.server.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation của NhatKyService.
 * Cung cấp cơ chế ghi audit log dùng chung cho mọi module.
 */
@Service
public class NhatKyServiceImpl implements NhatKyService {

    private static final Logger log = LoggerFactory.getLogger(NhatKyServiceImpl.class);
    private static final String ANONYMOUS = "ANONYMOUS";

    private final NhatKyHeThongRepository nhatKyRepository;

    public NhatKyServiceImpl(NhatKyHeThongRepository nhatKyRepository) {
        this.nhatKyRepository = nhatKyRepository;
    }

    @Override
    public void ghiNhatKy(HanhDong hanhDong, String bangMucTieu, Integer maMucTieu, HttpServletRequest request) {
        try {
            NguoiDung nguoiDungHienTai = layNguoiDungHienTai();

            String bangMucTieuGhi = (bangMucTieu == null || bangMucTieu.isBlank())
                    ? hanhDong.getBangMacDinh()
                    : bangMucTieu;

            NhatKyHeThong nhatKy = NhatKyHeThong.builder()
                    .vaiTroNguoiThucHien(nguoiDungHienTai != null
                            ? nguoiDungHienTai.getVaiTro().name()
                            : ANONYMOUS)
                    .maNguoiThucHien(nguoiDungHienTai != null
                            ? nguoiDungHienTai.getMa()
                            : null)
                    .hanhDong(hanhDong.name())
                    .bangMucTieu(bangMucTieuGhi)
                    .maMucTieu(maMucTieu)
                    .diaChiIp(layDiaChiIp(request))
                    .taoLuc(Instant.now())
                    .build();

            nhatKyRepository.save(nhatKy);
        } catch (Exception e) {
            log.error("Ghi nhat ky he thong that bai: hanhDong={}, bangMucTieu={}, maMucTieu={}",
                    hanhDong, bangMucTieu, maMucTieu, e);
        }
    }

    @Override
    public NguoiDung layNguoiDungHienTai() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getNguoiDung();
        }
        return null;
    }

    private String layDiaChiIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
