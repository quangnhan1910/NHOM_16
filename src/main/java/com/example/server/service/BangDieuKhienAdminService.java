package com.example.server.service;

import com.example.server.dto.NhatKyBangDieuKhienVm;
import com.example.server.dto.PhongThiHoatDongVm;
import com.example.server.model.CaThi;
import com.example.server.model.NguoiDung;
import com.example.server.model.NhatKyHeThong;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.model.enums.VaiTro;
import com.example.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gom số liệu thật cho trang bảng điều khiển quản trị viên (/admin).
 */
@Service
@Transactional(readOnly = true)
public class BangDieuKhienAdminService {

    private static final int SO_NGAY_THONG_KE_MOI = 30;
    private static final int SO_DONG_NHAT_KY = 5;
    private static final int SO_PHONG_TOI_DA = 5;

    private static final DateTimeFormatter DTF_LOG =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final KhoaRepository khoaRepository;
    private final SinhVienRepository sinhVienRepository;
    private final NganHangCauHoiRepository nganHangCauHoiRepository;
    private final CaThiRepository caThiRepository;
    private final NhatKyHeThongRepository nhatKyHeThongRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public BangDieuKhienAdminService(
            KhoaRepository khoaRepository,
            SinhVienRepository sinhVienRepository,
            NganHangCauHoiRepository nganHangCauHoiRepository,
            CaThiRepository caThiRepository,
            NhatKyHeThongRepository nhatKyHeThongRepository,
            NguoiDungRepository nguoiDungRepository) {
        this.khoaRepository = khoaRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.nganHangCauHoiRepository = nganHangCauHoiRepository;
        this.caThiRepository = caThiRepository;
        this.nhatKyHeThongRepository = nhatKyHeThongRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    public Map<String, Object> layDuLieuBangDieuKhien() {
        Instant tu30 = Instant.now().minus(SO_NGAY_THONG_KE_MOI, ChronoUnit.DAYS);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        long tongKhoa = khoaRepository.count();
        long khoaMoi = khoaRepository.demTaoTu(tu30);
        long tongSinhVien = sinhVienRepository.count();
        long svTaiKhoanMoi = nguoiDungRepository.countByVaiTroAndTaoLucGreaterThanEqual(VaiTro.SINH_VIEN, tu30);
        long tongCauHoi = nganHangCauHoiRepository.count();
        long cauHoiMoi = nganHangCauHoiRepository.demTaoTu(tu30);
        long caDangDienRa = caThiRepository.countByTrangThai(TrangThaiCaThi.DANG_DIEN_RA);

        long tongCa = caThiRepository.count();
        long choDangKy = caThiRepository.countByTrangThai(TrangThaiCaThi.CHO_DANG_KY);
        long daKetThuc = caThiRepository.countByTrangThai(TrangThaiCaThi.DA_KET_THUC);
        long daHuy = caThiRepository.countByTrangThai(TrangThaiCaThi.DA_HUY);

        int phanTramLan = phanTram(choDangKy, tongCa);
        int phanTramMayChu = phanTramKetThucThanhCong(daKetThuc, daHuy);

        Map<String, Object> m = new HashMap<>();
        m.put("tongKhoa", tongKhoa);
        m.put("tongKhoaFmt", nf.format(tongKhoa));
        m.put("khoaMoi30Ngay", khoaMoi);
        m.put("coKhoaMoi", khoaMoi > 0);

        m.put("tongSinhVien", tongSinhVien);
        m.put("tongSinhVienFmt", nf.format(tongSinhVien));
        m.put("sinhVienMoi30Ngay", svTaiKhoanMoi);
        m.put("coSinhVienMoi", svTaiKhoanMoi > 0);

        m.put("tongCauHoi", tongCauHoi);
        m.put("tongCauHoiFmt", nf.format(tongCauHoi));
        m.put("cauHoiMoi30Ngay", cauHoiMoi);
        m.put("coCauHoiMoi", cauHoiMoi > 0);

        m.put("caDangDienRa", caDangDienRa);
        m.put("caDangDienRaFmt", nf.format(caDangDienRa));

        List<NhatKyBangDieuKhienVm> nhatKyGanDay = buildNhatKyGanDay();
        m.put("nhatKyGanDay", nhatKyGanDay);
        m.put("coNhatKy", !nhatKyGanDay.isEmpty());

        List<PhongThiHoatDongVm> phongThiHoatDong = buildPhongThiHoatDong();
        m.put("phongThiHoatDong", phongThiHoatDong);
        m.put("coPhongThi", !phongThiHoatDong.isEmpty());

        m.put("sucKhoeMayChuPhanTram", phanTramMayChu);
        m.put("sucKhoeMayChuFooter1", "Tỷ lệ ca kết thúc bình thường (so với đã hủy)");
        m.put("sucKhoeMayChuFooter2", daKetThuc + " kết thúc · " + daHuy + " đã hủy");

        m.put("sucKhoeLanPhanTram", phanTramLan);
        m.put("sucKhoeLanFooter", "Ca chờ đăng ký: " + choDangKy + " · Tổng ca: " + tongCa);

        return m;
    }

    private static int phanTram(long tu, long mau) {
        if (mau <= 0) {
            return 100;
        }
        return (int) Math.min(100, Math.round(100.0 * tu / mau));
    }

    private static int phanTramKetThucThanhCong(long daKetThuc, long daHuy) {
        long mau = daKetThuc + daHuy;
        if (mau <= 0) {
            return 100;
        }
        return (int) Math.min(100, Math.round(100.0 * daKetThuc / mau));
    }

    private List<NhatKyBangDieuKhienVm> buildNhatKyGanDay() {
        List<NhatKyHeThong> rows = nhatKyHeThongRepository.findTop5ByOrderByTaoLucDesc();
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Integer> maNguoi = rows.stream()
                .map(NhatKyHeThong::getMaNguoiThucHien)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, NguoiDung> nguoiTheoMa = maNguoi.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findAllById(maNguoi).stream()
                        .collect(Collectors.toMap(NguoiDung::getMa, nd -> nd));

        List<NhatKyBangDieuKhienVm> out = new ArrayList<>();
        for (NhatKyHeThong nk : rows) {
            String tg = nk.getTaoLuc() != null ? DTF_LOG.format(nk.getTaoLuc()) : "—";
            String nguoi = tenNguoiThucHien(nk, nguoiTheoMa);
            String hdRaw = nk.getHanhDong() != null ? nk.getHanhDong() : "";
            String badge = badgeTuHanhDong(hdRaw);
            String chiTiet = chiTietNhatKy(nk);
            String ip = nk.getDiaChiIp() != null && !nk.getDiaChiIp().isBlank() ? nk.getDiaChiIp() : "—";
            out.add(new NhatKyBangDieuKhienVm(tg, nguoi, badge, hdRaw, chiTiet, ip));
        }
        return out;
    }

    private static String tenNguoiThucHien(NhatKyHeThong nk, Map<Integer, NguoiDung> nguoiTheoMa) {
        Integer ma = nk.getMaNguoiThucHien();
        if (ma == null) {
            return "Hệ thống";
        }
        NguoiDung nd = nguoiTheoMa.get(ma);
        if (nd == null) {
            return "Người dùng #" + ma;
        }
        String hoTen = nd.getHoTen() != null && !nd.getHoTen().isBlank()
                ? nd.getHoTen()
                : nd.getThuDienTu();
        String vaiTro = nd.getVaiTro() != null ? " (" + nd.getVaiTro().getHienThi() + ")" : "";
        return hoTen + vaiTro;
    }

    private static String chiTietNhatKy(NhatKyHeThong nk) {
        String moTa;
        try {
            moTa = HanhDong.valueOf(nk.getHanhDong()).getMoTa();
        } catch (Exception e) {
            moTa = nk.getHanhDong() != null ? nk.getHanhDong() : "—";
        }
        StringBuilder sb = new StringBuilder(moTa);
        if (nk.getMaMucTieu() != null) {
            sb.append(" · #").append(nk.getMaMucTieu());
        }
        return sb.toString();
    }

    /**
     * Ánh xạ thô từ mã hành động lưu DB → hậu tố class .log-action-badge.* trong admin.css.
     */
    private static String badgeTuHanhDong(String raw) {
        if (raw == null || raw.isBlank()) {
            return "add";
        }
        if (raw.contains("DANG_NHAP")) {
            return "start";
        }
        if (raw.contains("DANG_XUAT")) {
            return "backup";
        }
        if (raw.contains("XOA") || raw.contains("HUY") || raw.contains("KHOA_TAI_KHOAN")) {
            return "error";
        }
        if (raw.contains("DOI_MAT_KHAU") || raw.contains("SUA_")) {
            return "error";
        }
        if (raw.contains("KHAN_CAP")) {
            return "error";
        }
        if (raw.contains("THEM") || raw.contains("TAO_")) {
            return "add";
        }
        return "add";
    }

    private List<PhongThiHoatDongVm> buildPhongThiHoatDong() {
        List<CaThi> caList = caThiRepository.findByTrangThai(TrangThaiCaThi.DANG_DIEN_RA);
        if (caList.isEmpty()) {
            return List.of();
        }
        List<CaThi> top = caList.stream().limit(SO_PHONG_TOI_DA).toList();
        List<Integer> mas = top.stream().map(CaThi::getMa).toList();
        Map<Integer, Long> demDk = new HashMap<>();
        if (!mas.isEmpty()) {
            for (Object[] row : caThiRepository.demSoLuongDangKyTheoNhom(mas)) {
                demDk.put((Integer) row[0], (Long) row[1]);
            }
        }
        List<PhongThiHoatDongVm> out = new ArrayList<>();
        for (CaThi ct : top) {
            long dk = demDk.getOrDefault(ct.getMa(), 0L);
            Integer toiDa = ct.getSoLuongToiDa();
            String ten = tenPhong(ct);
            String dongPhai;
            String dot;
            if (toiDa != null && toiDa > 0) {
                dongPhai = dk + "/" + toiDa + " chỗ";
                dot = dk * 10 >= toiDa * 9 ? "yellow" : "green";
            } else {
                dongPhai = "Đã đăng ký: " + dk;
                dot = "green";
            }
            out.add(new PhongThiHoatDongVm(ten, dongPhai, dot));
        }
        return out;
    }

    private static String tenPhong(CaThi ct) {
        if (ct.getDiaDiem() != null && !ct.getDiaDiem().isBlank()) {
            return ct.getDiaDiem();
        }
        if (ct.getTenCaThi() != null && !ct.getTenCaThi().isBlank()) {
            return ct.getTenCaThi();
        }
        return "Ca #" + ct.getMa();
    }
}
