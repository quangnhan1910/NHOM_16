package com.example.server.service;

import com.example.server.dto.ChiTietKhoaHocVm;
import com.example.server.dto.LichGiangDayVm;
import com.example.server.model.*;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.CaThiRepository;
import com.example.server.repository.DangKyThiRepository;
import com.example.server.repository.GiangVienMonHocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChiTietKhoaHocService {

    private final GiangVienMonHocRepository giangVienMonHocRepository;
    private final CaThiRepository caThiRepository;
    private final DangKyThiRepository dangKyThiRepository;

    public ChiTietKhoaHocService(
            GiangVienMonHocRepository giangVienMonHocRepository,
            CaThiRepository caThiRepository,
            DangKyThiRepository dangKyThiRepository) {
        this.giangVienMonHocRepository = giangVienMonHocRepository;
        this.caThiRepository = caThiRepository;
        this.dangKyThiRepository = dangKyThiRepository;
    }

    /**
     * Lấy chi tiết khóa học theo mã Giảng Viên - Môn Học.
     */
    @Transactional(readOnly = true)
    public ChiTietKhoaHocVm layChiTietKhoaHoc(Integer maGiangVienMonHoc) {
        GiangVienMonHoc gvmh = giangVienMonHocRepository
                .findByMaWithMonHocVaSinhVienPhanCong(maGiangVienMonHoc)
                .orElse(null);

        if (gvmh == null || gvmh.getMonHoc() == null) {
            return null;
        }

        MonHoc monHoc = gvmh.getMonHoc();
        Integer maMonHoc = monHoc.getMa();
        Instant now = Instant.now();

        Long soLuongSv = 0L;
        try {
            soLuongSv = dangKyThiRepository.demSoLuongSinhVienTheoMonHoc(maMonHoc);
        } catch (Exception e) {
            // ignore
        }
        int soLuong = soLuongSv != null ? soLuongSv.intValue() : 0;
        if (gvmh.getSinhVienPhanCongs() != null) {
            for (GiangVienMonHocSinhVien l : gvmh.getSinhVienPhanCongs()) {
                SinhVien sv = l.getSinhVien();
                if (sv == null) {
                    continue;
                }
                try {
                    if (!dangKyThiRepository.coDangKyThiChoSinhVienVaMonHoc(sv.getMa(), maMonHoc)) {
                        soLuong++;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        // Lấy danh sách CaThi từ DB (lịch thi)
        List<CaThi> caThis = caThiRepository.findAll().stream()
                .filter(ct -> ct.getDeThi() != null && ct.getDeThi().getMonHoc() != null)
                .filter(ct -> ct.getDeThi().getMonHoc().getMa().equals(maMonHoc))
                .sorted((a, b) -> b.getThoiGianBatDau().compareTo(a.getThoiGianBatDau()))
                .collect(Collectors.toList());

        // Xác định trạng thái khóa học
        String trangThai = "LEN_KE_HOACH";
        if (!caThis.isEmpty()) {
            TrangThaiCaThi tt = caThis.get(0).getTrangThai();
            if (tt == TrangThaiCaThi.DANG_DIEN_RA) {
                trangThai = "DANG_DIEN_RA";
            } else if (tt == TrangThaiCaThi.CHO_DANG_KY) {
                Instant sapBatDau = caThis.get(0).getThoiGianBatDau();
                if (sapBatDau != null && sapBatDau.isAfter(now) &&
                    sapBatDau.isBefore(now.plusSeconds(7 * 24 * 60 * 60))) {
                    trangThai = "SAP_BAT_DAU";
                }
            } else if (tt == TrangThaiCaThi.DA_KET_THUC) {
                trangThai = "DA_KET_THUC";
            }
        }

        List<LichGiangDayVm> tuCaThi = caThis.stream()
                .map(this::chuyenDoiCaThiSangLichGiang)
                .collect(Collectors.toList());

        List<LichGiangDayVm> lichGiangDay = new ArrayList<>();
        if (coLichThuCongTuPhanCong(gvmh)) {
            lichGiangDay.add(taoLichThuCongTuGvmh(gvmh));
        }
        lichGiangDay.addAll(tuCaThi);

        List<String> tenParts = new ArrayList<>();
        List<String> mssvParts = new ArrayList<>();
        if (gvmh.getSinhVienPhanCongs() != null) {
            for (GiangVienMonHocSinhVien l : gvmh.getSinhVienPhanCongs()) {
                SinhVien sv = l.getSinhVien();
                if (sv == null) {
                    continue;
                }
                if (sv.getNguoiDung() != null && sv.getNguoiDung().getHoTen() != null) {
                    tenParts.add(sv.getNguoiDung().getHoTen());
                } else {
                    tenParts.add("—");
                }
                mssvParts.add(sv.getMaSinhVien() != null ? sv.getMaSinhVien() : "—");
            }
        }
        String tenSvGan = tenParts.isEmpty() ? null : String.join(", ", tenParts);
        String mssvGan = mssvParts.isEmpty() ? null : String.join(", ", mssvParts);

        return ChiTietKhoaHocVm.builder()
                .maMonHoc(maMonHoc)
                .tenMonHoc(monHoc.getTen() != null ? monHoc.getTen() : "Chưa đặt tên")
                .maDinhDanh(monHoc.getMaDinhDanh() != null ? monHoc.getMaDinhDanh() : "")
                .soTinChi(monHoc.getSoTinChi() != null ? monHoc.getSoTinChi() : 0)
                .soLuongSinhVien(soLuong)
                .lichHoc("Xem lịch thi bên dưới")
                .trangThai(trangThai)
                .lichGiangDay(lichGiangDay)
                .sinhVienGanKemHoTen(tenSvGan)
                .sinhVienGanKemMssv(mssvGan)
                .build();
    }

    /**
     * Chuyển đổi CaThi sang LichGiangDayVm.
     */
    private LichGiangDayVm chuyenDoiCaThiSangLichGiang(CaThi ct) {
        Instant thoiGianBatDau = ct.getThoiGianBatDau();
        Instant thoiGianKetThuc = ct.getThoiGianKetThuc();

        LocalDate ngay = thoiGianBatDau != null 
            ? thoiGianBatDau.atZone(ZoneId.systemDefault()).toLocalDate() 
            : null;
        LocalTime gioBatDau = thoiGianBatDau != null 
            ? thoiGianBatDau.atZone(ZoneId.systemDefault()).toLocalTime() 
            : null;
        LocalTime gioKetThuc = thoiGianKetThuc != null 
            ? thoiGianKetThuc.atZone(ZoneId.systemDefault()).toLocalTime() 
            : null;

        // Chuyển trạng thái CaThi sang trạng thái lịch
        String trangThaiLich = "CHUA_DAY";
        if (ct.getTrangThai() != null) {
            switch (ct.getTrangThai()) {
                case DA_KET_THUC:
                    trangThaiLich = "DA_DAY";
                    break;
                case DANG_DIEN_RA:
                    trangThaiLich = "SAI_TOI";
                    break;
                case CHO_DANG_KY:
                    trangThaiLich = "CHUA_DAY";
                    break;
                default:
                    trangThaiLich = "CHUA_DAY";
            }
        }

        return LichGiangDayVm.builder()
                .buoi(ct.getMa() != null ? String.valueOf(ct.getMa()) : null)
                .ngay(ngay)
                .thoiGianBatDau(gioBatDau)
                .thoiGianKetThuc(gioKetThuc)
                .tietBatDau(null)
                .tietKetThuc(null)
                .phong(ct.getDiaDiem() != null ? ct.getDiaDiem() : "Chưa xếp")
                .noiDung(ct.getTenCaThi() != null ? ct.getTenCaThi() : "Ca thi")
                .trangThai(trangThaiLich)
                .build();
    }

    private boolean coLichThuCongTuPhanCong(GiangVienMonHoc g) {
        if (g == null) {
            return false;
        }
        return (g.getLichBuoi() != null && !g.getLichBuoi().isBlank())
                || g.getLichNgay() != null
                || g.getLichTietBatDau() != null
                || g.getLichTietKetThuc() != null
                || (g.getLichPhong() != null && !g.getLichPhong().isBlank())
                || (g.getLichNoiDung() != null && !g.getLichNoiDung().isBlank())
                || (g.getLichTrangThai() != null && !g.getLichTrangThai().isBlank());
    }

    private LichGiangDayVm taoLichThuCongTuGvmh(GiangVienMonHoc g) {
        String buoi = (g.getLichBuoi() != null && !g.getLichBuoi().isBlank()) ? g.getLichBuoi().trim() : null;
        String phong = (g.getLichPhong() != null && !g.getLichPhong().isBlank()) ? g.getLichPhong().trim() : null;
        String noiDung = (g.getLichNoiDung() != null && !g.getLichNoiDung().isBlank()) ? g.getLichNoiDung().trim() : null;
        String tt = (g.getLichTrangThai() != null && !g.getLichTrangThai().isBlank())
                ? g.getLichTrangThai().trim()
                : "CHUA_DAY";
        return LichGiangDayVm.builder()
                .buoi(buoi)
                .ngay(g.getLichNgay())
                .thoiGianBatDau(null)
                .thoiGianKetThuc(null)
                .tietBatDau(g.getLichTietBatDau())
                .tietKetThuc(g.getLichTietKetThuc())
                .phong(phong)
                .noiDung(noiDung)
                .trangThai(tt)
                .build();
    }

    /**
     * Lấy danh sách chi tiết tất cả khóa học của giảng viên.
     */
    @Transactional(readOnly = true)
    public List<ChiTietKhoaHocVm> layDanhSachChiTietKhoaHoc(Integer maGiangVien) {
        List<GiangVienMonHoc> danhSach = giangVienMonHocRepository.findByGiangVienMa(maGiangVien);

        if (danhSach == null || danhSach.isEmpty()) {
            return List.of();
        }

        return danhSach.stream()
                .filter(gvmh -> gvmh.getMonHoc() != null)
                .map(gvmh -> layChiTietKhoaHoc(gvmh.getMa()))
                .collect(Collectors.toList());
    }
}
