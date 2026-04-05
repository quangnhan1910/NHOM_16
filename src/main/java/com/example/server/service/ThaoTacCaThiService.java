package com.example.server.service;

import com.example.server.model.CaThi;
import com.example.server.model.DangKyThi;
import com.example.server.model.enums.TrangThaiDangKy;
import com.example.server.repository.CaThiRepository;
import com.example.server.repository.DangKyThiRepository;
import com.example.server.repository.SinhVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ trang "Thao tác ca thi":
 * quản lý danh sách dự thi, check-in sinh viên, thêm/xóa thí sinh.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThaoTacCaThiService {

    private static final int PAGE_SIZE = 10;

    private final CaThiRepository     caThiRepository;
    private final DangKyThiRepository dangKyThiRepository;
    private final SinhVienRepository  sinhVienRepository;

    /**
     * Thống kê tổng hợp cho ca thi: tổng thí sinh, đã check-in, chưa check-in, máy trống.
     */
    public record ThongKe(int tongSo, int soCheckIn, int soChuaCheckIn, int soMayTrong,
                          int phanTramCheckIn) {
        public static ThongKe tinh(int tongSo, int soCheckIn, Integer soLuongToiDa) {
            int soChuaCheckIn = tongSo - soCheckIn;
            int soMayTrong    = soLuongToiDa != null ? Math.max(0, soLuongToiDa - soCheckIn) : 0;
            int phanTram      = tongSo > 0 ? (soCheckIn * 100 / tongSo) : 0;
            return new ThongKe(tongSo, soCheckIn, soChuaCheckIn, soMayTrong, phanTram);
        }
    }

    // ── Truy vấn ──────────────────────────────────────────────────────────────

    public Optional<CaThi> getCaThiByMa(Integer ma) {
        return caThiRepository.findChiTietByMa(ma);
    }

    /**
     * Lấy danh sách đăng ký thi có lọc và phân trang.
     *
     * @param daCheckIn null = tất cả, true = đã check-in, false = chưa check-in
     * @param tuKhoa    tìm theo MSSV hoặc họ tên (null/blank = bỏ qua)
     */
    public Page<DangKyThi> getDanhSachDangKy(Integer maCaThi, Boolean daCheckIn,
                                             String tuKhoa, int page) {
        String param = (tuKhoa != null && !tuKhoa.isBlank()) ? tuKhoa.trim() : null;
        return dangKyThiRepository.timTheoBoLocThaoTac(
                maCaThi, daCheckIn, param, PageRequest.of(page, PAGE_SIZE));
    }

    public ThongKe getThongKe(Integer maCaThi, CaThi caThi) {
        int tongSo    = dangKyThiRepository.demTong(maCaThi);
        int soCheckIn = dangKyThiRepository.demSoCheckIn(maCaThi);
        return ThongKe.tinh(tongSo, soCheckIn, caThi.getSoLuongToiDa());
    }

    // ── Check-in ──────────────────────────────────────────────────────────────

    /**
     * Bật/tắt trạng thái check-in của một đăng ký.
     * Khi check-in: ghi nhận checkInLuc = now().
     * Khi hủy check-in: xóa checkInLuc.
     */
    @Transactional
    public boolean toggleCheckIn(Integer maDangKy) {
        DangKyThi dk = dangKyThiRepository.findById(maDangKy)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đăng ký thi: " + maDangKy));
        boolean moiTrangThai = !Boolean.TRUE.equals(dk.getDaCheckIn());
        dk.setDaCheckIn(moiTrangThai);
        dk.setCheckInLuc(moiTrangThai ? Instant.now() : null);
        dangKyThiRepository.save(dk);
        return moiTrangThai;
    }

    // ── Quản lý danh sách ─────────────────────────────────────────────────────

    /** Xóa một sinh viên khỏi danh sách dự thi. */
    @Transactional
    public void xoaDangKy(Integer maDangKy) {
        if (!dangKyThiRepository.existsById(maDangKy)) {
            throw new IllegalArgumentException("Không tìm thấy đăng ký thi: " + maDangKy);
        }
        dangKyThiRepository.deleteById(maDangKy);
    }

    /**
     * Thêm một sinh viên vào danh sách dự thi theo MSSV.
     * Ném IllegalArgumentException nếu MSSV không tồn tại hoặc đã đăng ký.
     */
    @Transactional
    public DangKyThi themSinhVien(Integer maCaThi, String maSinhVienInput) {
        CaThi caThi = caThiRepository.findById(maCaThi)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy ca thi: " + maCaThi));

        var sinhVien = sinhVienRepository.findByMaSinhVien(maSinhVienInput.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sinh viên có MSSV: " + maSinhVienInput.trim()));

        dangKyThiRepository.findByCaThiMaAndSinhVienMa(maCaThi, sinhVien.getMa())
                .ifPresent(dk -> {
                    throw new IllegalArgumentException(
                            "Sinh viên " + maSinhVienInput.trim() + " đã có trong danh sách ca thi này");
                });

        DangKyThi dk = DangKyThi.builder()
                .caThi(caThi)
                .sinhVien(sinhVien)
                .trangThaiDangKy(TrangThaiDangKy.DA_XAC_NHAN)
                .dangKyLuc(Instant.now())
                .daCheckIn(false)
                .build();
        return dangKyThiRepository.save(dk);
    }
}
