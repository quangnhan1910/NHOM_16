package com.example.server.service;

import com.example.server.dto.KetQuaThiSinhVienVm;
import com.example.server.dto.ThongKeCaThiVm;
import com.example.server.dto.ThongKeDiemVm;
import com.example.server.dto.ThongKeTongHopVm;
import com.example.server.model.BaiThiSinhVien;
import com.example.server.model.CaThi;
import com.example.server.model.CauHoiDeThi;
import com.example.server.model.DeThi;
import com.example.server.model.enums.MucDoKho;
import com.example.server.model.enums.TrangThaiBaiThi;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.BaiThiSinhVienRepository;
import com.example.server.repository.CaThiRepository;
import com.example.server.repository.CauHoiDeThiRepository;
import com.example.server.repository.CauTraLoiSinhVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    private final CaThiRepository caThiRepository;
    private final BaiThiSinhVienRepository baiThiSinhVienRepository;
    private final CauHoiDeThiRepository cauHoiDeThiRepository;
    private final CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Điểm đạt mặc định (thang 10) khi đề thi chưa cấu hình {@code de_thi.diem_dat}. */
    private static final BigDecimal NGUONG_DIEM_DAT_MAC_DINH = BigDecimal.valueOf(4);

    private static final List<TrangThaiBaiThi> TRANG_THAI_DA_NOP = List.of(
            TrangThaiBaiThi.DA_NOP, TrangThaiBaiThi.DA_CHAM);
    private static final List<TrangThaiCaThi> TRANG_THAI_KET_THUC = List.of(
            TrangThaiCaThi.DA_KET_THUC);

    public ThongKeService(CaThiRepository caThiRepository,
                          BaiThiSinhVienRepository baiThiSinhVienRepository,
                          CauHoiDeThiRepository cauHoiDeThiRepository,
                          CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository) {
        this.caThiRepository = caThiRepository;
        this.baiThiSinhVienRepository = baiThiSinhVienRepository;
        this.cauHoiDeThiRepository = cauHoiDeThiRepository;
        this.cauTraLoiSinhVienRepository = cauTraLoiSinhVienRepository;
    }

    /**
     * Phản hồi rỗng khi tài khoản không gắn hồ sơ giảng viên hoặc chưa có dữ liệu.
     */
    public ThongKeTongHopVm layTongHopThongKeRong() {
        ThongKeDiemVm diemRong = ThongKeDiemVm.builder()
                .diemTrungBinh(BigDecimal.ZERO)
                .tiLeDat(BigDecimal.ZERO)
                .tiLeRot(BigDecimal.ZERO)
                .tongSinhVien(0)
                .soDat(0)
                .soRot(0)
                .soVang(0)
                .build();
        return ThongKeTongHopVm.builder()
                .danhSachCaThi(List.of())
                .tongCaThi(0)
                .tongSinhVien(0)
                .tongDaNop(0)
                .tongVang(0)
                .diemTongThe(diemRong)
                .build();
    }

    @Transactional(readOnly = true)
    public ThongKeTongHopVm layTongHopThongKe(Integer maGiangVien) {
        List<CaThi> dsCaThi = caThiRepository.findByGiangVienMaAndTrangThaiIn(
                maGiangVien, TRANG_THAI_KET_THUC);

        List<ThongKeCaThiVm> danhSachThongKe = dsCaThi.stream()
                .map(this::chuyenSangThongKeCaThiVm)
                .collect(Collectors.toList());

        ThongKeDiemVm diemTongThe = tinhDiemTongThe(maGiangVien);

        int tongSv = Optional.ofNullable(
                        baiThiSinhVienRepository.demTongBaiThiGiangVien(
                                maGiangVien, TRANG_THAI_DA_NOP, TRANG_THAI_KET_THUC))
                .orElse(0);
        int tongVang = baiThiSinhVienRepository.demBaiThiGiangVienTheoTrangThai(
                maGiangVien, TrangThaiBaiThi.DANG_LAM, TRANG_THAI_KET_THUC);

        return ThongKeTongHopVm.builder()
                .danhSachCaThi(danhSachThongKe)
                .tongCaThi(dsCaThi.size())
                .tongSinhVien(tongSv)
                .tongDaNop(tongSv)
                .tongVang(tongVang)
                .diemTongThe(diemTongThe)
                .build();
    }

    @Transactional(readOnly = true)
    public ThongKeCaThiVm layThongKeChiTiet(Integer maCaThi) {
        CaThi caThi = caThiRepository.findByMaWithDetails(maCaThi);
        if (caThi == null) {
            return null;
        }
        return chuyenSangThongKeCaThiVm(caThi);
    }

    @Transactional(readOnly = true)
    public List<KetQuaThiSinhVienVm> layKetQuaThiSinhVien(Integer maCaThi) {
        List<BaiThiSinhVien> dsBaiThi = baiThiSinhVienRepository.findByCaThiMaWithDetails(maCaThi);
        return dsBaiThi.stream()
                .filter(bts -> bts.getTrangThai() == TrangThaiBaiThi.DA_NOP
                            || bts.getTrangThai() == TrangThaiBaiThi.DA_CHAM)
                .map(this::chuyenSangKetQuaVm)
                .collect(Collectors.toList());
    }

    private ThongKeCaThiVm chuyenSangThongKeCaThiVm(CaThi caThi) {
        Integer maCaThi = caThi.getMa();
        Integer maDeThi = caThi.getDeThi().getMa();

        Integer tongSoSv = baiThiSinhVienRepository.demTongBaiThi(maCaThi);
        Integer soDaNop = baiThiSinhVienRepository.demBaiThiDaNopTheoCaThi(maCaThi, TRANG_THAI_DA_NOP);

        Integer soDat = baiThiSinhVienRepository.demBaiThiDatTheoCaThi(
                maCaThi, true, TRANG_THAI_DA_NOP, NGUONG_DIEM_DAT_MAC_DINH);
        Integer soRot = baiThiSinhVienRepository.demBaiThiDatTheoCaThi(
                maCaThi, false, TRANG_THAI_DA_NOP, NGUONG_DIEM_DAT_MAC_DINH);
        Integer soVang = tongSoSv - soDaNop;

        Double avgDiem = baiThiSinhVienRepository.tinhDiemTrungBinhTheoCaThi(maCaThi, TRANG_THAI_DA_NOP);
        BigDecimal diemTrungBinh = avgDiem != null
                ? BigDecimal.valueOf(avgDiem).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal tiLeDat = BigDecimal.ZERO;
        if (soDaNop > 0) {
            tiLeDat = BigDecimal.valueOf(soDat)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(soDaNop), 1, RoundingMode.HALF_UP);
        }

        Integer cauHoiKhoNhatMa = null;
        String cauHoiKhoNhatNoiDung = null;
        BigDecimal tiLeTraLoiDungKho = BigDecimal.ZERO;

        List<CauHoiDeThi> dsCauHoi = cauHoiDeThiRepository.findByDeThiMaOrderByDiemAsc(maDeThi);
        if (!dsCauHoi.isEmpty()) {
            CauHoiDeThi cauHoiKho = dsCauHoi.get(dsCauHoi.size() - 1);
            if (cauHoiKho.getCauHoi() != null && cauHoiKho.getCauHoi().getMucDoKho() == MucDoKho.KHO) {
                cauHoiKhoNhatMa = cauHoiKho.getMa();
                cauHoiKhoNhatNoiDung = cauHoiKho.getCauHoi().getNoiDung();
                if (cauHoiKhoNhatNoiDung != null && cauHoiKhoNhatNoiDung.length() > 50) {
                    cauHoiKhoNhatNoiDung = cauHoiKhoNhatNoiDung.substring(0, 47) + "...";
                }

                Integer soTraLoiDung = cauTraLoiSinhVienRepository.demSoSinhVienTraLoiDungTheoCauHoi(cauHoiKho.getMa());
                Integer tongTraLoi = cauTraLoiSinhVienRepository.demTongSoSinhVienTraLoiTheoCauHoi(cauHoiKho.getMa());
                if (tongTraLoi > 0) {
                    tiLeTraLoiDungKho = BigDecimal.valueOf(soTraLoiDung)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(tongTraLoi), 1, RoundingMode.HALF_UP);
                }
            }
        }

        LocalDateTime gioBatDau = convertInstant(caThi.getThoiGianBatDau());
        LocalDateTime gioKetThuc = convertInstant(caThi.getThoiGianKetThuc());

        String thoiGianHienThi = String.format("Ca %s (%s - %s)",
                layThuCuaNgay(gioBatDau),
                gioBatDau != null ? gioBatDau.format(TIME_FMT) : "",
                gioKetThuc != null ? gioKetThuc.format(TIME_FMT) : "");

        return ThongKeCaThiVm.builder()
                .maCaThi(maCaThi)
                .tenCaThi(caThi.getTenCaThi())
                .tenMonHoc(caThi.getDeThi().getMonHoc().getTen())
                .maDinhDanhMonHoc(caThi.getDeThi().getMonHoc().getMaDinhDanh())
                .soTinChi(caThi.getDeThi().getMonHoc().getSoTinChi())
                .thoiGianBatDau(gioBatDau)
                .thoiGianKetThuc(gioKetThuc)
                .thoiGianHienThi(thoiGianHienThi)
                .diaDiem(caThi.getDiaDiem())
                .trangThai(caThi.getTrangThai().name())
                .tongSoSinhVien(tongSoSv != null ? tongSoSv : 0)
                .soDaNop(soDaNop != null ? soDaNop : 0)
                .soVang(soVang != null && soVang > 0 ? soVang : 0)
                .soDat(soDat != null ? soDat : 0)
                .soRot(soRot != null ? soRot : 0)
                .diemTrungBinh(diemTrungBinh)
                .tiLeDat(tiLeDat)
                .cauHoiKhoNhatMa(cauHoiKhoNhatMa)
                .cauHoiKhoNhatNoiDung(cauHoiKhoNhatNoiDung)
                .tiLeTraLoiDungCauHoiKho(tiLeTraLoiDungKho)
                .build();
    }

    private ThongKeDiemVm tinhDiemTongThe(Integer maGiangVien) {
        Integer tongSv = baiThiSinhVienRepository.demTongBaiThiGiangVien(
                maGiangVien, TRANG_THAI_DA_NOP, TRANG_THAI_KET_THUC);
        if (tongSv == null) {
            tongSv = 0;
        }
        Integer soDat = baiThiSinhVienRepository.demBaiThiGiangVienTheoKetQuaDat(
                maGiangVien, TRANG_THAI_DA_NOP, TRANG_THAI_KET_THUC, true, NGUONG_DIEM_DAT_MAC_DINH);
        if (soDat == null) {
            soDat = 0;
        }
        int soRot = tongSv - soDat;

        Double avgDiem = baiThiSinhVienRepository.tinhDiemTrungBinhGiangVien(
                maGiangVien, TRANG_THAI_DA_NOP, TRANG_THAI_KET_THUC);
        BigDecimal diemTrungBinh = avgDiem != null
                ? BigDecimal.valueOf(avgDiem).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal tiLeDat = BigDecimal.ZERO;
        BigDecimal tiLeRot = BigDecimal.ZERO;
        if (tongSv != null && tongSv > 0) {
            tiLeDat = BigDecimal.valueOf(soDat)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(tongSv), 1, RoundingMode.HALF_UP);
            tiLeRot = BigDecimal.valueOf(100).subtract(tiLeDat);
        }

        Integer soVang = baiThiSinhVienRepository.demBaiThiGiangVienTheoTrangThai(
                maGiangVien, TrangThaiBaiThi.DANG_LAM, TRANG_THAI_KET_THUC);
        if (soVang == null) soVang = 0;

        return ThongKeDiemVm.builder()
                .diemTrungBinh(diemTrungBinh)
                .tiLeDat(tiLeDat)
                .tiLeRot(tiLeRot)
                .tongSinhVien(tongSv != null ? tongSv : 0)
                .soDat(soDat)
                .soRot(soRot)
                .soVang(soVang)
                .build();
    }

    private KetQuaThiSinhVienVm chuyenSangKetQuaVm(BaiThiSinhVien bts) {
        String lopSh = "";
        if (bts.getSinhVien().getChuyenNganh() != null) {
            lopSh = bts.getSinhVien().getChuyenNganh().getTen();
        }

        BigDecimal diem = bts.getTongDiem() != null ? bts.getTongDiem() : BigDecimal.ZERO;
        String xepLoai = xepLoai(diem);

        Integer thoiGianPhut = null;
        if (bts.getThoiGianLamBaiGiay() != null) {
            thoiGianPhut = bts.getThoiGianLamBaiGiay() / 60;
        }

        return KetQuaThiSinhVienVm.builder()
                .maBaiThi(bts.getMa())
                .maSinhVien(bts.getSinhVien().getMa())
                .maSinhVienStr(bts.getSinhVien().getMaSinhVien())
                .hoVaTen(bts.getSinhVien().getNguoiDung().getHoTen())
                .lopSinhHoat(lopSh)
                .thoiGianNop(convertInstant(bts.getNopBaiLuc()))
                .thoiGianLamBaiPhut(thoiGianPhut)
                .tongDiem(diem)
                .dat(tinhLaDatTheoDiemVaDeThi(bts))
                .xepLoai(xepLoai)
                .build();
    }

    /** Đạt khi điểm (thang 10) đạt ngưỡng đề ({@code de_thi.diem_dat}), mặc định 4.0. */
    private boolean tinhLaDatTheoDiemVaDeThi(BaiThiSinhVien bts) {
        BigDecimal diem = bts.getTongDiem();
        if (diem == null) {
            return false;
        }
        BigDecimal nguong = NGUONG_DIEM_DAT_MAC_DINH;
        if (bts.getCaThi() != null && bts.getCaThi().getDeThi() != null) {
            DeThi dt = bts.getCaThi().getDeThi();
            if (dt.getDiemDat() != null) {
                nguong = dt.getDiemDat();
            }
        }
        return diem.compareTo(nguong) >= 0;
    }

    private String xepLoai(BigDecimal diem) {
        if (diem == null) return "YEU";
        if (diem.compareTo(BigDecimal.valueOf(9)) >= 0) return "XUAT_SAC";
        if (diem.compareTo(BigDecimal.valueOf(8)) >= 0) return "KHA";
        if (diem.compareTo(BigDecimal.valueOf(6.5)) >= 0) return "TRUNG_BINH";
        if (diem.compareTo(BigDecimal.valueOf(5)) >= 0) return "YEU";
        return "KEM";
    }

    private LocalDateTime convertInstant(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String layThuCuaNgay(LocalDateTime dt) {
        if (dt == null) return "";
        String[] thu = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        return thu[dt.getDayOfWeek().getValue() % 7];
    }
}
