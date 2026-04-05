package com.example.server.service;

import com.example.server.dto.DeThiDTO;
import com.example.server.dto.LuuDeThiRequest;
import com.example.server.model.*;
import com.example.server.model.enums.LoaiCauHoi;
import com.example.server.model.enums.VaiTro;
import com.example.server.repository.*;
import com.example.server.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ cho đề thi.
 */
@Service
@RequiredArgsConstructor
public class DeThiService {

    private final DeThiRepository deThiRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final GiangVienMonHocRepository giangVienMonHocRepository;
    private final NganHangCauHoiRepository nganHangCauHoiRepository;

    /**
     * Lấy GiangVien hiện tại từ SecurityContextHolder (session đăng nhập).
     */
    private GiangVien layGiangVienHienTai() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa đăng nhập.");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("Không xác định được người dùng hiện tại.");
        }
        NguoiDung nguoiDung = userDetails.getNguoiDung();
        return giangVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                .orElseThrow(() -> new RuntimeException(
                        "Người dùng hiện tại không phải giảng viên."));
    }

    /**
     * Lấy danh sách đề thi có phân trang, filter trạng thái, và tìm kiếm theo tên.
     */
    @Transactional(readOnly = true)
    public Page<DeThiDTO> danhSach(Boolean daXuatBan, String tuKhoa, Pageable pageable) {
        Page<DeThi> page = deThiRepository.timKiem(daXuatBan, tuKhoa, pageable);
        return page.map(this::toDTO);
    }

    /**
     * Đếm số lượng đề thi theo trạng thái để hiển thị trên tabs.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> demTheoTrangThai() {
        Map<String, Long> result = new HashMap<>();
        long total = deThiRepository.count();
        long published = deThiRepository.countByDaXuatBan(true);
        long draft = deThiRepository.countByDaXuatBan(false);

        result.put("total", total);
        result.put("published", published);
        result.put("draft", draft);
        return result;
    }

    /**
     * Lấy danh sách tất cả môn học (cho dropdown).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> layDanhSachMonHoc() {
        return monHocRepository.findAll().stream()
                .map(mh -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("ma", mh.getMa());
                    map.put("ten", mh.getTen());
                    map.put("maDinhDanh", mh.getMaDinhDanh());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Danh sách môn cho form tạo đề thi: quản trị xem tất cả; giảng viên chỉ môn có trong phân công
     * {@code giang_vien_mon_hoc}.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> layDanhSachMonHocChoFormDeThi() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return List.of();
        }
        VaiTro vaiTro = userDetails.getNguoiDung().getVaiTro();
        if (vaiTro == VaiTro.QUAN_TRI_VIEN) {
            return layDanhSachMonHoc().stream()
                    .sorted(Comparator.comparing(m -> Objects.toString(m.get("ten"), ""),
                            String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
        if (vaiTro != VaiTro.GIANG_VIEN) {
            return List.of();
        }
        GiangVien gv = giangVienRepository.findByNguoiDungMa(userDetails.getNguoiDung().getMa())
                .orElse(null);
        if (gv == null) {
            return List.of();
        }
        List<GiangVienMonHoc> phanCong = giangVienMonHocRepository.findByGiangVienMa(gv.getMa());
        Map<Integer, MonHoc> monTheoMa = new LinkedHashMap<>();
        for (GiangVienMonHoc g : phanCong) {
            if (g.getMonHoc() != null) {
                monTheoMa.putIfAbsent(g.getMonHoc().getMa(), g.getMonHoc());
            }
        }
        return monTheoMa.values().stream()
                .sorted(Comparator.comparing(MonHoc::getTen, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(mh -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("ma", mh.getMa());
                    map.put("ten", mh.getTen());
                    map.put("maDinhDanh", mh.getMaDinhDanh() != null ? mh.getMaDinhDanh() : "");
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Tạo đề thi mới (lưu bản nháp).
     */
    @Transactional
    public DeThiDTO taoDeThi(String tenDeThi, Integer maMonHoc, Integer thoiLuongPhut) {
        MonHoc monHoc = monHocRepository.findById(maMonHoc)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã: " + maMonHoc));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud
                ? cud
                : null;

        // Lấy giảng viên từ session đăng nhập thực tế
        GiangVien giangVien = layGiangVienHienTai();

        if (userDetails != null && userDetails.getNguoiDung().getVaiTro() == VaiTro.GIANG_VIEN) {
            if (!giangVienMonHocRepository.existsByGiangVienMaAndMonHocMa(giangVien.getMa(), maMonHoc)) {
                throw new RuntimeException(
                        "Bạn chỉ được tạo đề thi cho các môn học được phân công giảng dạy.");
            }
        }

        Instant now = Instant.now();

        DeThi deThi = DeThi.builder()
                .tenDeThi(tenDeThi)
                .monHoc(monHoc)
                .giangVien(giangVien)
                .thoiLuongPhut(thoiLuongPhut)
                .daXuatBan(false) // Bản nháp
                .xaoTronCauHoi(false)
                .xaoTronLuaChon(false)
                .taoLuc(now)
                .capNhatLuc(now)
                .build();

        DeThi saved = deThiRepository.save(deThi);
        return toDTO(saved);
    }

    /**
     * Lưu đề thi: tạo câu hỏi vào NganHangCauHoi + link vào CauHoiDeThi + cập nhật tổng điểm.
     */
    @Transactional
    public DeThiDTO luuDeThi(LuuDeThiRequest request) {
        if (request.getMaDeThiHienTai() == null) {
            throw new RuntimeException("Thiếu mã đề thi.");
        }

        DeThi deThi = deThiRepository.findById(request.getMaDeThiHienTai())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với mã: " + request.getMaDeThiHienTai()));

        // Xóa câu hỏi cũ (nếu có) để thay bằng câu mới
        deThi.getCauHoiDeThis().clear();

        GiangVien giangVien = deThi.getGiangVien();
        MonHoc monHoc = deThi.getMonHoc();
        Instant now = Instant.now();

        BigDecimal tongDiem = BigDecimal.ZERO;
        String[] optionLabels = {"A", "B", "C", "D", "E", "F", "G"};

        if (request.getCauHois() != null) {
            for (int i = 0; i < request.getCauHois().size(); i++) {
                LuuDeThiRequest.CauHoiDTO cauHoiDTO = request.getCauHois().get(i);

                // 1. Tạo câu hỏi trong ngân hàng câu hỏi
                LoaiCauHoi loaiCauHoi = cauHoiDTO.isMultipleChoice()
                        ? LoaiCauHoi.TRAC_NGHIEM_NHIEU_DAP_AN
                        : LoaiCauHoi.TRAC_NGHIEM_1_DAP_AN;

                NganHangCauHoi cauHoi = NganHangCauHoi.builder()
                        .monHoc(monHoc)
                        .giangVien(giangVien)
                        .noiDung(cauHoiDTO.getNoiDung())
                        .loaiCauHoi(loaiCauHoi)
                        .diem(cauHoiDTO.getDiem())
                        .trangThaiHoatDong(true)
                        .taoLuc(now)
                        .capNhatLuc(now)
                        .build();

                // 2. Tạo lựa chọn cho câu hỏi
                if (cauHoiDTO.getLuaChons() != null) {
                    for (int j = 0; j < cauHoiDTO.getLuaChons().size(); j++) {
                        LuuDeThiRequest.LuaChonDTO luaChonDTO = cauHoiDTO.getLuaChons().get(j);

                        LuaChonCauHoi luaChon = LuaChonCauHoi.builder()
                                .cauHoi(cauHoi)
                                .nhanLuaChon(luaChonDTO.getNhan() != null ? luaChonDTO.getNhan() : optionLabels[j])
                                .noiDungLuaChon(luaChonDTO.getNoiDung())
                                .laDapAnDung(luaChonDTO.isLaDapAnDung())
                                .thuTuHienThi(j + 1)
                                .build();

                        cauHoi.getLuaChonCauHois().add(luaChon);
                    }
                }

                // Lưu câu hỏi vào DB
                NganHangCauHoi savedCauHoi = nganHangCauHoiRepository.save(cauHoi);

                // 3. Tạo liên kết CauHoiDeThi
                BigDecimal diemCauHoi = cauHoiDTO.getDiem() != null ? cauHoiDTO.getDiem() : BigDecimal.ZERO;

                CauHoiDeThi cauHoiDeThi = CauHoiDeThi.builder()
                        .deThi(deThi)
                        .cauHoi(savedCauHoi)
                        .thuTuCauHoi(i + 1)
                        .diem(diemCauHoi)
                        .build();

                deThi.getCauHoiDeThis().add(cauHoiDeThi);
                tongDiem = tongDiem.add(diemCauHoi);
            }
        }

        // 4. Cập nhật tổng điểm và thời gian
        if (request.getTongDiem() != null) {
            deThi.setTongDiem(request.getTongDiem());
        } else {
            deThi.setTongDiem(tongDiem);
        }
        deThi.setDaXuatBan(true);
        deThi.setCapNhatLuc(now);

        DeThi saved = deThiRepository.save(deThi);
        return toDTO(saved);
    }

    /**
     * Xóa đề thi theo mã.
     */
    @Transactional
    public void xoaDeThi(Integer ma) {
        DeThi deThi = deThiRepository.findById(ma)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với mã: " + ma));
        deThiRepository.delete(deThi);
    }

    /**
     * Lấy chi tiết đề thi gồm câu hỏi + lựa chọn dạng rawText.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> layChiTiet(Integer ma) {
        DeThi deThi = deThiRepository.findById(ma)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với mã: " + ma));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ma", deThi.getMa());
        result.put("tenDeThi", deThi.getTenDeThi());
        result.put("tenMonHoc", deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : null);
        result.put("tongDiem", deThi.getTongDiem());

        // Chuyển câu hỏi + lựa chọn thành rawText
        StringBuilder sb = new StringBuilder();
        List<CauHoiDeThi> cauHoiDeThis = deThi.getCauHoiDeThis();
        if (cauHoiDeThis != null) {
            // Sắp xếp theo thứ tự câu hỏi
            cauHoiDeThis.sort((a, b) -> {
                int orderA = a.getThuTuCauHoi() != null ? a.getThuTuCauHoi() : 0;
                int orderB = b.getThuTuCauHoi() != null ? b.getThuTuCauHoi() : 0;
                return Integer.compare(orderA, orderB);
            });

            for (int i = 0; i < cauHoiDeThis.size(); i++) {
                CauHoiDeThi chdt = cauHoiDeThis.get(i);
                NganHangCauHoi cauHoi = chdt.getCauHoi();
                if (cauHoi == null) continue;

                // Câu hỏi
                sb.append("Câu ").append(i + 1).append(": ").append(cauHoi.getNoiDung()).append("\n");

                // Lựa chọn
                List<LuaChonCauHoi> luaChons = cauHoi.getLuaChonCauHois();
                if (luaChons != null) {
                    luaChons.sort((a, b) -> {
                        int orderA = a.getThuTuHienThi() != null ? a.getThuTuHienThi() : 0;
                        int orderB = b.getThuTuHienThi() != null ? b.getThuTuHienThi() : 0;
                        return Integer.compare(orderA, orderB);
                    });

                    for (LuaChonCauHoi lc : luaChons) {
                        String prefix = lc.getNhanLuaChon() != null ? lc.getNhanLuaChon() : "";
                        String marker = Boolean.TRUE.equals(lc.getLaDapAnDung()) ? "*" : "";
                        sb.append(marker).append(prefix).append(". ").append(lc.getNoiDungLuaChon()).append("\n");
                    }
                }

                sb.append("\n");
            }
        }

        result.put("rawText", sb.toString().trim());
        return result;
    }

    /**
     * Chuyển Entity DeThi sang DTO.
     */
    private DeThiDTO toDTO(DeThi entity) {
        return DeThiDTO.builder()
                .ma(entity.getMa())
                .tenDeThi(entity.getTenDeThi())
                .tenMonHoc(entity.getMonHoc() != null ? entity.getMonHoc().getTen() : null)
                .maMonHoc(entity.getMonHoc() != null ? entity.getMonHoc().getMa() : null)
                .loaiDeThi(entity.getLoaiDeThi() != null ? entity.getLoaiDeThi().name() : null)
                .thoiLuongPhut(entity.getThoiLuongPhut())
                .tongDiem(entity.getTongDiem())
                .daXuatBan(entity.getDaXuatBan())
                .soCauHoi(entity.getCauHoiDeThis() != null ? entity.getCauHoiDeThis().size() : 0)
                .taoLuc(entity.getTaoLuc())
                .capNhatLuc(entity.getCapNhatLuc())
                .build();
    }
}

