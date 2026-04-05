package com.example.server.service;

import com.example.server.model.GiangVien;
import com.example.server.model.Khoa;
import com.example.server.model.NguoiDung;
import com.example.server.model.enums.VaiTro;
import com.example.server.repository.GiangVienRepository;
import com.example.server.repository.KhoaRepository;
import com.example.server.repository.NguoiDungRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tự tạo bản ghi {@code giang_vien} khi {@code nguoi_dung} có vai trò GIANG_VIEN nhưng chưa có hồ sơ.
 * Dùng khoa đầu tiên (theo mã) và mã nhân viên hệ thống dạng SYSGV_{ma}.
 */
@Service
public class GiangVienHoSoTuDongService {

    private final NguoiDungRepository nguoiDungRepository;
    private final GiangVienRepository giangVienRepository;
    private final KhoaRepository khoaRepository;

    public GiangVienHoSoTuDongService(
            NguoiDungRepository nguoiDungRepository,
            GiangVienRepository giangVienRepository,
            KhoaRepository khoaRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.giangVienRepository = giangVienRepository;
        this.khoaRepository = khoaRepository;
    }

    @Transactional
    public void damBaoHoSoChoTatCaTaiKhoanGiangVien() {
        List<Khoa> khoas = khoaRepository.findAll(Sort.by(Sort.Direction.ASC, "ma"));
        if (khoas.isEmpty()) {
            return;
        }
        Khoa khoaMacDinh = khoas.get(0);
        List<NguoiDung> taiKhoanGv = nguoiDungRepository.findByVaiTro(VaiTro.GIANG_VIEN);
        for (NguoiDung nd : taiKhoanGv) {
            taoHoSoNeuThieu(nd, khoaMacDinh);
        }
    }

    /**
     * Dùng khi giảng viên đăng nhập: chỉ bổ sung hồ sơ cho chính tài khoản đó.
     */
    @Transactional
    public void damBaoHoSoChoNguoiDungHienTai(NguoiDung nd) {
        if (nd == null || nd.getVaiTro() != VaiTro.GIANG_VIEN) {
            return;
        }
        if (Boolean.FALSE.equals(nd.getTrangThaiHoatDong())) {
            return;
        }
        if (giangVienRepository.findByNguoiDungMa(nd.getMa()).isPresent()) {
            return;
        }
        List<Khoa> khoas = khoaRepository.findAll(Sort.by(Sort.Direction.ASC, "ma"));
        if (khoas.isEmpty()) {
            return;
        }
        taoHoSoNeuThieu(nd, khoas.get(0));
    }

    private void taoHoSoNeuThieu(NguoiDung nd, Khoa khoaMacDinh) {
        if (Boolean.FALSE.equals(nd.getTrangThaiHoatDong())) {
            return;
        }
        if (giangVienRepository.findByNguoiDungMa(nd.getMa()).isPresent()) {
            return;
        }
        String maNv = sinhMaNhanVien(nd.getMa());
        GiangVien gv = GiangVien.builder()
                .nguoiDung(nd)
                .khoa(khoaMacDinh)
                .maNhanVien(maNv)
                .build();
        giangVienRepository.save(gv);
    }

    private String sinhMaNhanVien(Integer maNguoiDung) {
        String base = "SYSGV_" + maNguoiDung;
        String candidate = base;
        int i = 0;
        while (giangVienRepository.existsByMaNhanVien(candidate)) {
            i++;
            candidate = base + "_" + i;
        }
        return candidate;
    }
}
