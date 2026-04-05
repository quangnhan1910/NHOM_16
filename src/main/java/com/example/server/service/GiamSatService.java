package com.example.server.service;

import com.example.server.dto.*;
import com.example.server.model.*;
import com.example.server.model.enums.TrangThaiBaiThi;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GiamSatService {

    private final CaThiRepository caThiRepository;
    private final BaiThiSinhVienRepository baiThiSinhVienRepository;
    private final DangKyThiRepository dangKyThiRepository;
    private final CauHoiDeThiRepository cauHoiDeThiRepository;
    private final CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository;
    private final KhoaHocCuaToiService khoaHocCuaToiService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GiamSatService(CaThiRepository caThiRepository,
                          BaiThiSinhVienRepository baiThiSinhVienRepository,
                          DangKyThiRepository dangKyThiRepository,
                          CauHoiDeThiRepository cauHoiDeThiRepository,
                          CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository,
                          KhoaHocCuaToiService khoaHocCuaToiService) {
        this.caThiRepository = caThiRepository;
        this.baiThiSinhVienRepository = baiThiSinhVienRepository;
        this.dangKyThiRepository = dangKyThiRepository;
        this.cauHoiDeThiRepository = cauHoiDeThiRepository;
        this.cauTraLoiSinhVienRepository = cauTraLoiSinhVienRepository;
        this.khoaHocCuaToiService = khoaHocCuaToiService;
    }

    /**
     * Lấy tổng hợp giám sát cho 1 giảng viên (các ca thi đang diễn ra).
     */
    @Transactional(readOnly = true)
    public GiamSatTongHopVm layTongHopGiamSat(Integer maGiangVien) {
        List<CaThi> dsCaThi = caThiRepository.findByGiangVienMaAndTrangThai(
            maGiangVien, TrangThaiCaThi.DANG_DIEN_RA);

        List<GiamSatCaThiVm> danhSachVm = dsCaThi.stream()
            .map(this::chuyenSangVm)
            .collect(Collectors.toList());

        Integer tongSinhVien = baiThiSinhVienRepository
            .demTongSinhVienDangThiCuaGiangVien(maGiangVien);

        return GiamSatTongHopVm.builder()
            .danhSachCaThi(danhSachVm)
            .tongCaThi(dsCaThi.size())
            .tongSinhVienDangThi(tongSinhVien != null ? tongSinhVien : 0)
            .build();
    }

    /**
     * Lấy tổng hợp giám sát tất cả ca thi đang diễn ra (cho admin).
     */
    @Transactional(readOnly = true)
    public GiamSatTongHopVm layTongHopGiamSatTatCa() {
        List<CaThi> dsCaThi = caThiRepository
            .findByTrangThai(TrangThaiCaThi.DANG_DIEN_RA);

        List<GiamSatCaThiVm> danhSachVm = dsCaThi.stream()
            .map(this::chuyenSangVm)
            .collect(Collectors.toList());

        int tongSv = danhSachVm.stream()
            .mapToInt(GiamSatCaThiVm::getSoDangLam)
            .sum();

        return GiamSatTongHopVm.builder()
            .danhSachCaThi(danhSachVm)
            .tongCaThi(dsCaThi.size())
            .tongSinhVienDangThi(tongSv)
            .build();
    }

    /**
     * Lấy chi tiết 1 ca thi để giám sát (cập nhật real-time).
     */
    @Transactional(readOnly = true)
    public GiamSatCaThiVm layChiTietGiamSat(Integer maCaThi) {
        CaThi caThi = caThiRepository.findByMaWithDetails(maCaThi);
        if (caThi == null) {
            return null;
        }
        return chuyenSangVm(caThi);
    }

    /**
     * Chi tiết bài thi một sinh viên để giảng viên theo dõi (read-only).
     *
     * @param maCaThi nếu truyền phải khớp ca thi của bài thi; dùng để tránh lệch ngữ cảnh từ URL
     */
    @Transactional(readOnly = true)
    public GiamSatTheoDoiBaiVm layTheoDoiBaiThi(Integer maBaiThi, Integer maCaThi) {
        BaiThiSinhVien bts = baiThiSinhVienRepository.findByMaForTheoDoi(maBaiThi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài thi"));
        CaThi caThi = bts.getCaThi();
        if (caThi == null || caThi.getMa() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ca thi");
        }
        if (maCaThi != null && !maCaThi.equals(caThi.getMa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã ca thi không khớp với bài thi");
        }
        damBaoQuyenTheoDoi(bts);

        DeThi deThi = caThi.getDeThi();
        if (deThi == null || deThi.getMa() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đề thi");
        }

        Integer tongCauHoi = cauHoiDeThiRepository.demSoCauHoi(deThi.getMa());
        List<CauTraLoiSinhVien> traLois = new ArrayList<>(
                cauTraLoiSinhVienRepository.findChiTietByBaiThiMa(maBaiThi));
        Map<Integer, CauTraLoiSinhVien> traLoiTheoMaCauHoiDeThi = traLois.stream()
                .filter(c -> c.getCauHoiDeThi() != null && c.getCauHoiDeThi().getMa() != null)
                .collect(Collectors.toMap(
                        c -> c.getCauHoiDeThi().getMa(),
                        c -> c,
                        (a, b) -> a));

        List<CauHoiDeThi> tatCaCauTrongDe = cauHoiDeThiRepository.findByDeThiMaForClient(deThi.getMa());
        List<GiamSatTheoDoiCauVm> chiTiet = new ArrayList<>();
        for (CauHoiDeThi chdt : tatCaCauTrongDe) {
            if (chdt.getCauHoi() == null) {
                continue;
            }
            CauTraLoiSinhVien ctl = chdt.getMa() != null
                    ? traLoiTheoMaCauHoiDeThi.get(chdt.getMa())
                    : null;
            chiTiet.add(chuyenMotCauTheoDoi(chdt, ctl));
        }

        Integer soCauDaTraLoi = cauTraLoiSinhVienRepository.demSoCauDaTraLoi(maBaiThi);
        boolean canhBao = bts.getTrangThai() == TrangThaiBaiThi.DANG_LAM && traLois.isEmpty();

        String lopSh = "";
        if (bts.getSinhVien().getChuyenNganh() != null) {
            lopSh = bts.getSinhVien().getChuyenNganh().getTen();
        }

        return GiamSatTheoDoiBaiVm.builder()
                .maBaiThi(bts.getMa())
                .maCaThi(caThi.getMa())
                .tenCaThi(caThi.getTenCaThi())
                .tenMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : null)
                .maDinhDanhMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getMaDinhDanh() : null)
                .maSinhVienStr(bts.getSinhVien().getMaSinhVien())
                .hoVaTen(bts.getSinhVien().getNguoiDung().getHoTen())
                .lopSinhHoat(lopSh)
                .batDauLuc(convertInstant(bts.getBatDauLuc()))
                .nopBaiLuc(convertInstant(bts.getNopBaiLuc()))
                .trangThai(bts.getTrangThai().name())
                .tongDiem(bts.getTongDiem())
                .soCauDaTraLoi(soCauDaTraLoi != null ? soCauDaTraLoi : 0)
                .tongCauHoi(tongCauHoi != null ? tongCauHoi : 0)
                .canhBaoChuaLuuTraLoiLenServer(canhBao)
                .chiTietCauTraLoi(chiTiet)
                .build();
    }

    private GiamSatTheoDoiCauVm chuyenMotCauTheoDoi(CauHoiDeThi chdt, CauTraLoiSinhVien ctl) {
        List<GiamSatTheoDoiLuaChonVm> daChon = new ArrayList<>();
        if (ctl != null && ctl.getLuaChonCauTraLois() != null) {
            for (LuaChonCauTraLoi lctl : ctl.getLuaChonCauTraLois()) {
                if (lctl.getLuaChon() == null || lctl.getLuaChon().getMa() == null) {
                    continue;
                }
                daChon.add(GiamSatTheoDoiLuaChonVm.builder()
                        .maLuaChon(lctl.getLuaChon().getMa())
                        .noiDungLuaChon(lctl.getLuaChon().getNoiDungLuaChon())
                        .build());
            }
        }
        Boolean laDung = ctl != null ? ctl.getLaDapAnDung() : null;
        return GiamSatTheoDoiCauVm.builder()
                .maCauHoiDeThi(chdt.getMa())
                .noiDungCauHoi(chdt.getCauHoi().getNoiDung())
                .laDapAnDung(laDung)
                .luaChonDaChon(daChon)
                .build();
    }

    private void damBaoQuyenTheoDoi(BaiThiSinhVien bts) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xem bài thi này");
        }
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_QUAN_TRI_VIEN".equals(a.getAuthority()));
        if (admin) {
            return;
        }
        Integer maGvDn = khoaHocCuaToiService.layMaGiangVienDangNhap().orElse(null);
        DeThi dt = bts.getCaThi() != null ? bts.getCaThi().getDeThi() : null;
        if (dt == null || dt.getGiangVien() == null || dt.getGiangVien().getMa() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xem bài thi này");
        }
        if (maGvDn == null || !maGvDn.equals(dt.getGiangVien().getMa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xem bài thi này");
        }
    }

    private GiamSatCaThiVm chuyenSangVm(CaThi caThi) {
        Integer maDeThi = caThi.getDeThi().getMa();
        Integer maCaThiVal = caThi.getMa();

        List<DangKyThi> dsDangKy = dangKyThiRepository.findByCaThiMaWithDetails(maCaThiVal);
        List<BaiThiSinhVien> dsBaiThi = baiThiSinhVienRepository
            .findByCaThiMaWithDetails(maCaThiVal);

        Integer tongSoSv = dangKyThiRepository.demSoDangKy(maCaThiVal);
        Integer soDaVao = baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DANG_LAM)
            + baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DA_NOP)
            + baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DA_CHAM);
        Integer soDangLam = baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DANG_LAM);
        Integer soDaNop = baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DA_NOP)
            + baiThiSinhVienRepository.demBaiThiTheoTrangThai(
            maCaThiVal, TrangThaiBaiThi.DA_CHAM);

        Integer tongCauHoi = cauHoiDeThiRepository.demSoCauHoi(maDeThi);

        Map<Integer, BaiThiSinhVien> baiThiTheoSinhVien = dsBaiThi.stream()
            .filter(bts -> bts.getSinhVien() != null && bts.getSinhVien().getMa() != null)
            .collect(Collectors.toMap(
                bts -> bts.getSinhVien().getMa(),
                bts -> bts,
                (a, b) -> a,
                LinkedHashMap::new
            ));

        List<GiamSatSinhVienVm> dsSinhVien = dsDangKy.stream()
            .map(dk -> {
                Integer maSinhVien = dk.getSinhVien() != null ? dk.getSinhVien().getMa() : null;
                BaiThiSinhVien baiThi = maSinhVien != null ? baiThiTheoSinhVien.get(maSinhVien) : null;
                if (baiThi != null) {
                    return chuyenSinhVienSangVm(baiThi, tongCauHoi);
                }
                return chuyenDangKySangVm(dk, tongCauHoi);
            })
            .sorted(Comparator.comparing(
                GiamSatSinhVienVm::getMaSinhVienStr,
                Comparator.nullsLast(String::compareTo)
            ))
            .collect(Collectors.toList());

        LocalDateTime gioBatDau = convertInstant(caThi.getThoiGianBatDau());
        LocalDateTime gioKetThuc = convertInstant(caThi.getThoiGianKetThuc());

        String thoiGianHienThi = String.format("Ca %s (%s - %s)",
            layThuCuaNgay(gioBatDau),
            gioBatDau.format(TIME_FMT),
            gioKetThuc.format(TIME_FMT));

        return GiamSatCaThiVm.builder()
            .maCaThi(maCaThiVal)
            .tenCaThi(caThi.getTenCaThi())
            .tenMonHoc(caThi.getDeThi().getMonHoc().getTen())
            .maDinhDanhMonHoc(caThi.getDeThi().getMonHoc().getMaDinhDanh())
            .soTinChi(caThi.getDeThi().getMonHoc().getSoTinChi())
            .maCaThiNumber(maCaThiVal)
            .thoiGianBatDau(gioBatDau)
            .thoiGianKetThuc(gioKetThuc)
            .thoiGianHienThi(thoiGianHienThi)
            .phongMay(caThi.getDiaDiem())
            .diaDiem(caThi.getDiaDiem())
            .trangThai(caThi.getTrangThai().name())
            .tongSoSinhVien(tongSoSv != null ? tongSoSv : 0)
            .soDaVao(soDaVao != null ? soDaVao : 0)
            .soDangLam(soDangLam != null ? soDangLam : 0)
            .soDaNop(soDaNop != null ? soDaNop : 0)
            .danhSachSinhVien(dsSinhVien)
            .build();
    }

    private GiamSatSinhVienVm chuyenSinhVienSangVm(BaiThiSinhVien bts, Integer tongCauHoi) {
        Integer maBaiThi = bts.getMa();
        Integer soCauDaTraLoi = cauTraLoiSinhVienRepository.demSoCauDaTraLoi(maBaiThi);

        String lopSh = "";
        if (bts.getSinhVien().getChuyenNganh() != null) {
            lopSh = bts.getSinhVien().getChuyenNganh().getTen();
        }

        return GiamSatSinhVienVm.builder()
            .maBaiThi(maBaiThi)
            .maSinhVien(bts.getSinhVien().getMa())
            .maSinhVienStr(bts.getSinhVien().getMaSinhVien())
            .hoVaTen(bts.getSinhVien().getNguoiDung().getHoTen())
            .lopSinhHoat(lopSh)
            .batDauLuc(convertInstant(bts.getBatDauLuc()))
            .nopBaiLuc(convertInstant(bts.getNopBaiLuc()))
            .thoiGianLamBaiPhut(bts.getThoiGianLamBaiGiay())
            .trangThai(bts.getTrangThai().name())
            .tongDiem(bts.getTongDiem())
            .diaChiIp(bts.getDiaChiIp())
            .daCoMatKhau(bts.getSinhVien().getNguoiDung().getMatKhauMaHoa() != null)
            .soCauDaTraLoi(soCauDaTraLoi != null ? soCauDaTraLoi : 0)
            .tongCauHoi(tongCauHoi != null ? tongCauHoi : 0)
            .build();
    }

    private GiamSatSinhVienVm chuyenDangKySangVm(DangKyThi dk, Integer tongCauHoi) {
        String lopSh = "";
        if (dk.getSinhVien() != null && dk.getSinhVien().getChuyenNganh() != null) {
            lopSh = dk.getSinhVien().getChuyenNganh().getTen();
        }

        return GiamSatSinhVienVm.builder()
            .maBaiThi(null)
            .maSinhVien(dk.getSinhVien() != null ? dk.getSinhVien().getMa() : null)
            .maSinhVienStr(dk.getSinhVien() != null ? dk.getSinhVien().getMaSinhVien() : null)
            .hoVaTen(
                dk.getSinhVien() != null && dk.getSinhVien().getNguoiDung() != null
                    ? dk.getSinhVien().getNguoiDung().getHoTen()
                    : null
            )
            .lopSinhHoat(lopSh)
            .batDauLuc(null)
            .nopBaiLuc(null)
            .thoiGianLamBaiPhut(null)
            .trangThai("CHUA_VAO")
            .tongDiem(null)
            .diaChiIp(null)
            .daCoMatKhau(
                dk.getSinhVien() != null
                    && dk.getSinhVien().getNguoiDung() != null
                    && dk.getSinhVien().getNguoiDung().getMatKhauMaHoa() != null
            )
            .soCauDaTraLoi(0)
            .tongCauHoi(tongCauHoi != null ? tongCauHoi : 0)
            .build();
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
