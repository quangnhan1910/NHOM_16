package com.example.server.service;

import com.example.server.dto.KhoaHocCuaToiVm;
import com.example.server.model.CaThi;
import com.example.server.model.GiangVien;
import com.example.server.model.GiangVienMonHoc;
import com.example.server.model.NguoiDung;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.CaThiRepository;
import com.example.server.repository.DangKyThiRepository;
import com.example.server.repository.GiangVienMonHocRepository;
import com.example.server.repository.GiangVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KhoaHocCuaToiService {

    private final GiangVienMonHocRepository giangVienMonHocRepository;
    private final CaThiRepository caThiRepository;
    private final DangKyThiRepository dangKyThiRepository;
    private final NhatKyService nhatKyService;
    private final GiangVienRepository giangVienRepository;
    private final GiangVienHoSoTuDongService giangVienHoSoTuDongService;

    public KhoaHocCuaToiService(
            GiangVienMonHocRepository giangVienMonHocRepository,
            CaThiRepository caThiRepository,
            DangKyThiRepository dangKyThiRepository,
            NhatKyService nhatKyService,
            GiangVienRepository giangVienRepository,
            GiangVienHoSoTuDongService giangVienHoSoTuDongService) {
        this.giangVienMonHocRepository = giangVienMonHocRepository;
        this.caThiRepository = caThiRepository;
        this.dangKyThiRepository = dangKyThiRepository;
        this.nhatKyService = nhatKyService;
        this.giangVienRepository = giangVienRepository;
        this.giangVienHoSoTuDongService = giangVienHoSoTuDongService;
    }

    /**
     * Mã giảng viên tương ứng tài khoản đang đăng nhập (bảng giang_vien.ma_nguoi_dung).
     */
    public Optional<Integer> layMaGiangVienDangNhap() {
        NguoiDung nd = nhatKyService.layNguoiDungHienTai();
        if (nd == null) {
            return Optional.empty();
        }
        giangVienHoSoTuDongService.damBaoHoSoChoNguoiDungHienTai(nd);
        return giangVienRepository.findByNguoiDungMa(nd.getMa()).map(GiangVien::getMa);
    }

    /**
     * Danh sách khóa học của giảng viên đang đăng nhập.
     */
    public List<KhoaHocCuaToiVm> layDanhSachKhoaHocCuaGiangVienDangNhap() {
        return layMaGiangVienDangNhap()
                .map(this::layDanhSachKhoaHocCuaGiangVien)
                .orElse(List.of());
    }

    /**
     * Lấy danh sách khóa học của giảng viên.
     * Hiện tại lấy tất cả các môn học mà giảng viên được phân công.
     * Transaction: cần vì truy cập LAZY (GiangVienMonHoc.monHoc, CaThi.deThi, DeThi.monHoc)
     * khi {@code spring.jpa.open-in-view=false}.
     */
    @Transactional(readOnly = true)
    public List<KhoaHocCuaToiVm> layDanhSachKhoaHocCuaGiangVien(Integer maGiangVien) {
        List<GiangVienMonHoc> danhSach = giangVienMonHocRepository.findByGiangVienMa(maGiangVien);

        if (danhSach == null || danhSach.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now();

        return danhSach.stream()
                .filter(gvmh -> gvmh.getMonHoc() != null)
                .map(gvmh -> {
                    var monHoc = gvmh.getMonHoc();
                    Integer maMonHoc = monHoc.getMa();

                    // Đếm số sinh viên đã đăng ký thi môn này
                    Long soLuongSv = 0L;
                    try {
                        soLuongSv = dangKyThiRepository.demSoLuongSinhVienTheoMonHoc(maMonHoc);
                    } catch (Exception e) {
                        // Nếu lỗi thì gán = 0
                    }
                    Integer soLuong = soLuongSv != null ? soLuongSv.intValue() : 0;

                    // Tìm ca thi gần nhất để xác định trạng thái
                    List<CaThi> caThis = caThiRepository.findAll().stream()
                            .filter(ct -> ct.getDeThi() != null && ct.getDeThi().getMonHoc() != null)
                            .filter(ct -> ct.getDeThi().getMonHoc().getMa().equals(maMonHoc))
                            .sorted((a, b) -> b.getThoiGianBatDau().compareTo(a.getThoiGianBatDau()))
                            .toList();

                    String trangThai;
                    Instant thoiGianBatDau = null;
                    Instant thoiGianKetThuc = null;

                    if (!caThis.isEmpty()) {
                        CaThi caThiGanNhat = caThis.get(0);
                        TrangThaiCaThi tt = caThiGanNhat.getTrangThai();

                        if (tt == TrangThaiCaThi.DANG_DIEN_RA) {
                            trangThai = "DANG_DIEN_RA";
                            thoiGianBatDau = caThiGanNhat.getThoiGianBatDau();
                            thoiGianKetThuc = caThiGanNhat.getThoiGianKetThuc();
                        } else if (tt == TrangThaiCaThi.CHO_DANG_KY) {
                            // Kiểm tra nếu sắp bắt đầu trong vòng 7 ngày thì là SAP_BAT_DAU
                            Instant sapBatDau = caThiGanNhat.getThoiGianBatDau();
                            if (sapBatDau != null && sapBatDau.isAfter(now) &&
                                sapBatDau.isBefore(now.plusSeconds(7 * 24 * 60 * 60))) {
                                trangThai = "SAP_BAT_DAU";
                            } else {
                                trangThai = "LEN_KE_HOACH";
                            }
                            thoiGianBatDau = sapBatDau;
                            thoiGianKetThuc = caThiGanNhat.getThoiGianKetThuc();
                        } else if (tt == TrangThaiCaThi.DA_KET_THUC) {
                            // Đã kết thúc, kiểm tra các ca thi tiếp theo
                            CaThi caThiTiep = caThis.stream()
                                    .filter(ct -> ct.getThoiGianBatDau().isAfter(now))
                                    .findFirst()
                                    .orElse(null);
                            if (caThiTiep != null) {
                                trangThai = "SAP_BAT_DAU";
                                thoiGianBatDau = caThiTiep.getThoiGianBatDau();
                                thoiGianKetThuc = caThiTiep.getThoiGianKetThuc();
                            } else {
                                trangThai = "LEN_KE_HOACH";
                            }
                        } else {
                            trangThai = "LEN_KE_HOACH";
                        }
                    } else {
                        trangThai = "LEN_KE_HOACH";
                    }

                    // Tạo lịch học giả lập (vì database không có bảng lịch học)
                    String lichHoc = taoLichHocGiaLap(monHoc.getMa());

                    return KhoaHocCuaToiVm.builder()
                            .maMonHoc(maMonHoc)
                            .tenMonHoc(monHoc.getTen() != null ? monHoc.getTen() : "Chưa đặt tên")
                            .maDinhDanh(monHoc.getMaDinhDanh() != null ? monHoc.getMaDinhDanh() : "")
                            .soTinChi(monHoc.getSoTinChi() != null ? monHoc.getSoTinChi() : 0)
                            .soLuongSinhVien(soLuong)
                            .lichHoc(lichHoc)
                            .trangThai(trangThai)
                            .thoiGianBatDau(thoiGianBatDau)
                            .thoiGianKetThuc(thoiGianKetThuc)
                            .maGiangVienMonHoc(gvmh.getMa())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Tạo lịch học giả lập dựa trên mã môn học.
     * Trong thực tế, cần có bảng lịch học riêng.
     */
    private String taoLichHocGiaLap(Integer maMonHoc) {
        // Các môn khác nhau sẽ có lịch khác nhau
        String[] thu = {"Thứ 2, 4", "Thứ 3, 5", "Thứ 6", "Thứ 7", "Chưa xếp"};
        String[] ca = {"(Sáng)", "(Chiều)", "(Cả ngày)"};

        int idxThu = maMonHoc % thu.length;
        int idxCa = maMonHoc % ca.length;

        return thu[idxThu] + " " + ca[idxCa];
    }
}
