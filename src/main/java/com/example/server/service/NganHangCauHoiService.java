package com.example.server.service;

import com.example.server.dto.ImportCauHoiRequest;
import com.example.server.dto.NganHangCauHoiDTO;
import com.example.server.model.*;
import com.example.server.model.enums.LoaiCauHoi;
import com.example.server.model.enums.MucDoKho;
import com.example.server.repository.GiangVienRepository;
import com.example.server.repository.MonHocRepository;
import com.example.server.repository.NganHangCauHoiRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý logic nghiệp vụ cho ngân hàng câu hỏi.
 */
@Service
@RequiredArgsConstructor
public class NganHangCauHoiService {

    private final NganHangCauHoiRepository nganHangCauHoiRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;

    /**
     * Lấy danh sách câu hỏi có phân trang và filter.
     */
    @Transactional(readOnly = true)
    public Page<NganHangCauHoiDTO> danhSach(Integer maMonHoc,
                                             LoaiCauHoi loaiCauHoi,
                                             MucDoKho mucDoKho,
                                             String tuKhoa,
                                             Pageable pageable) {
        Page<NganHangCauHoi> page = nganHangCauHoiRepository.timKiem(
                maMonHoc, loaiCauHoi, mucDoKho, tuKhoa, pageable
        );
        return page.map(this::toDTO);
    }

    /**
     * Lấy chi tiết 1 câu hỏi theo mã.
     */
    @Transactional(readOnly = true)
    public NganHangCauHoiDTO layTheoMa(Integer ma) {
        NganHangCauHoi entity = nganHangCauHoiRepository.findById(ma)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi với mã: " + ma));
        return toDTO(entity);
    }

    /**
     * Xóa câu hỏi theo mã.
     */
    @Transactional
    public void xoa(Integer ma) {
        if (!nganHangCauHoiRepository.existsById(ma)) {
            throw new RuntimeException("Không tìm thấy câu hỏi với mã: " + ma);
        }
        nganHangCauHoiRepository.deleteById(ma);
    }

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
     * Import hàng loạt câu hỏi từ file đã parse.
     * Trả về số câu hỏi đã lưu thành công.
     */
    @Transactional
    public int importCauHoi(ImportCauHoiRequest request) {
        MonHoc monHoc = monHocRepository.findById(request.getMaMonHoc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã: " + request.getMaMonHoc()));

        // Lấy giảng viên từ session đăng nhập
        GiangVien giangVien = layGiangVienHienTai();

        String[] optionLabels = {"A", "B", "C", "D", "E", "F", "G"};
        List<NganHangCauHoi> entities = new ArrayList<>();
        Instant now = Instant.now();

        for (ImportCauHoiRequest.CauHoiItem item : request.getCauHois()) {
            // Xác định loại câu hỏi
            LoaiCauHoi loaiCauHoi = Boolean.TRUE.equals(item.getMultipleChoice())
                    ? LoaiCauHoi.TRAC_NGHIEM_NHIEU_DAP_AN
                    : LoaiCauHoi.TRAC_NGHIEM_1_DAP_AN;

            // Parse mức độ khó
            MucDoKho mucDoKho = MucDoKho.TRUNG_BINH;
            try {
                if (item.getMucDoKho() != null && !item.getMucDoKho().isBlank()) {
                    mucDoKho = MucDoKho.valueOf(item.getMucDoKho());
                }
            } catch (IllegalArgumentException ignored) {}

            NganHangCauHoi cauHoi = NganHangCauHoi.builder()
                    .monHoc(monHoc)
                    .giangVien(giangVien)
                    .noiDung(item.getQuestionText())
                    .loaiCauHoi(loaiCauHoi)
                    .mucDoKho(mucDoKho)
                    .diem(item.getDiem() != null ? BigDecimal.valueOf(item.getDiem()) : BigDecimal.ONE)
                    .trangThaiHoatDong(true)
                    .taoLuc(now)
                    .capNhatLuc(now)
                    .build();

            // Tạo lựa chọn
            if (item.getOptions() != null) {
                for (int i = 0; i < item.getOptions().size(); i++) {
                    ImportCauHoiRequest.OptionItem opt = item.getOptions().get(i);
                    LuaChonCauHoi luaChon = LuaChonCauHoi.builder()
                            .cauHoi(cauHoi)
                            .nhanLuaChon(i < optionLabels.length ? optionLabels[i] : String.valueOf(i + 1))
                            .noiDungLuaChon(opt.getText())
                            .laDapAnDung(Boolean.TRUE.equals(opt.getCorrect()))
                            .thuTuHienThi(i + 1)
                            .build();
                    cauHoi.getLuaChonCauHois().add(luaChon);
                }
            }

            entities.add(cauHoi);
        }

        nganHangCauHoiRepository.saveAll(entities);
        return entities.size();
    }

    /**
     * Chuyển Entity sang DTO.
     */
    private NganHangCauHoiDTO toDTO(NganHangCauHoi entity) {
        return NganHangCauHoiDTO.builder()
                .ma(entity.getMa())
                .noiDung(entity.getNoiDung())
                .maMonHoc(entity.getMonHoc() != null ? entity.getMonHoc().getMa() : null)
                .tenMonHoc(entity.getMonHoc() != null ? entity.getMonHoc().getTen() : null)
                .loaiCauHoi(entity.getLoaiCauHoi() != null ? entity.getLoaiCauHoi().name() : null)
                .mucDoKho(entity.getMucDoKho() != null ? entity.getMucDoKho().name() : null)
                .diem(entity.getDiem())
                .trangThaiHoatDong(entity.getTrangThaiHoatDong())
                .build();
    }
}

