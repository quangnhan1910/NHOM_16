package com.example.server.service;

import com.example.server.dto.CaThiFormDTO;
import com.example.server.model.*;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.model.enums.TrangThaiDangKy;
import com.example.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ liên quan đến Ca Thi.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaThiService {

    private static final int PAGE_SIZE = 10;
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CaThiRepository caThiRepository;
    private final KhoaRepository  khoaRepository;
    private final DeThiRepository  deThiRepository;
    private final DangKyThiRepository dangKyThiRepository;
    private final SinhVienRepository sinhVienRepository;

    // ── Truy vấn ──────────────────────────────────────────────────────────────

    /**
     * Lấy trang ca thi, lọc tại DB level, không load toàn bộ bảng vào bộ nhớ.
     */
    public Page<CaThi> getDanhSachCaThi(int page, TrangThaiCaThi trangThai, Integer maKhoa) {
        return caThiRepository.timTheoBoLoc(trangThai, maKhoa, PageRequest.of(page, PAGE_SIZE));
    }

    /** Tất cả khoa để hiển thị dropdown bộ lọc và form. */
    public List<Khoa> getDanhSachKhoa() {
        return khoaRepository.findAll();
    }

    /** Tất cả đề thi để hiển thị dropdown form. */
    public List<DeThi> getDanhSachDeThi() {
        return deThiRepository.findAll();
    }

    /**
     * Lấy chi tiết ca thi kèm các association cần thiết cho trang chi tiết.
     */
    public Optional<CaThi> getCaThiByMa(Integer ma) {
        return caThiRepository.findChiTietByMa(ma);
    }

    /**
     * Đếm số lượng đăng ký thi theo từng ca thi trong danh sách – batch query, tránh N+1.
     * Trả về Map<maCaThi, soLuongDangKy>.
     */
    public Map<Integer, Integer> getDemSoLuongDangKy(List<CaThi> caThis) {
        if (caThis.isEmpty()) return Map.of();
        List<Integer> ids = caThis.stream().map(CaThi::getMa).collect(Collectors.toList());
        List<Object[]> rows = caThiRepository.demSoLuongDangKyTheoNhom(ids);
        Map<Integer, Integer> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((Integer) row[0], ((Long) row[1]).intValue());
        }
        return counts;
    }

    /** Đếm số bài thi đã nộp trong một ca thi. */
    public int demSoBaiThiDaNop(Integer maCaThi) {
        return caThiRepository.demSoBaiThiDaNop(maCaThi);
    }

    // ── Tạo mới ───────────────────────────────────────────────────────────────

    @Transactional
    public CaThi taoCaThi(CaThiFormDTO dto) {
        DeThi deThi = deThiRepository.findById(dto.getMaDeThi())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Đề thi không tồn tại: " + dto.getMaDeThi()));
        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khoa không tồn tại: " + dto.getMaKhoa()));

        Instant now = Instant.now();
        CaThi caThi = CaThi.builder()
                .deThi(deThi)
                .khoa(khoa)
                .tenCaThi(dto.getTenCaThi().trim())
                .thoiGianBatDau(toInstant(dto.getThoiGianBatDau()))
                .thoiGianKetThuc(toInstant(dto.getThoiGianKetThuc()))
                .diaDiem(dto.getDiaDiem() != null ? dto.getDiaDiem().trim() : null)
                .soLuongToiDa(dto.getSoLuongToiDa())
                .trangThai(dto.getTrangThai())
                .taoLuc(now)
                .capNhatLuc(now)
                .build();

        return caThiRepository.save(caThi);
    }

    @Transactional
    public CaThi taoCaThi(CaThiFormDTO dto, MultipartFile fileSinhVien) {
        CaThi caThi = taoCaThi(dto);
        List<String> danhSachMssv = docMssvTuExcel(fileSinhVien);
        if (!danhSachMssv.isEmpty()) {
            themDanhSachSinhVien(caThi, danhSachMssv, false);
        }
        return caThi;
    }

    // ── Cập nhật ──────────────────────────────────────────────────────────────

    @Transactional
    public CaThi capNhatCaThi(Integer ma, CaThiFormDTO dto) {
        CaThi caThi = caThiRepository.findById(ma)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy ca thi: " + ma));
        DeThi deThi = deThiRepository.findById(dto.getMaDeThi())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Đề thi không tồn tại: " + dto.getMaDeThi()));
        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khoa không tồn tại: " + dto.getMaKhoa()));

        caThi.setDeThi(deThi);
        caThi.setKhoa(khoa);
        caThi.setTenCaThi(dto.getTenCaThi().trim());
        caThi.setThoiGianBatDau(toInstant(dto.getThoiGianBatDau()));
        caThi.setThoiGianKetThuc(toInstant(dto.getThoiGianKetThuc()));
        caThi.setDiaDiem(dto.getDiaDiem() != null ? dto.getDiaDiem().trim() : null);
        caThi.setSoLuongToiDa(dto.getSoLuongToiDa());
        caThi.setTrangThai(dto.getTrangThai());
        caThi.setCapNhatLuc(Instant.now());

        return caThiRepository.save(caThi);
    }

    @Transactional
    public CaThi capNhatCaThi(Integer ma, CaThiFormDTO dto, MultipartFile fileSinhVien) {
        CaThi caThi = capNhatCaThi(ma, dto);
        List<String> danhSachMssv = docMssvTuExcel(fileSinhVien);
        if (!danhSachMssv.isEmpty()) {
            themDanhSachSinhVien(caThi, danhSachMssv, true);
        }
        return caThi;
    }

    // ── Kích hoạt / Kết thúc nhanh ────────────────────────────────────────────

    /**
     * Thay đổi trạng thái ca thi thành DANG_DIEN_RA (giám thị kích hoạt).
     * Chỉ cho phép khi ca thi đang ở trạng thái CHO_DANG_KY.
     */
    @Transactional
    public CaThi kichHoatCaThi(Integer ma) {
        CaThi caThi = caThiRepository.findById(ma)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca thi với mã: " + ma));
        if (caThi.getTrangThai() != TrangThaiCaThi.CHO_DANG_KY) {
            throw new IllegalStateException(
                    "Chỉ có thể kích hoạt ca thi đang ở trạng thái 'Chờ đăng ký'. Trạng thái hiện tại: "
                    + caThi.getTrangThai());
        }
        caThi.setTrangThai(TrangThaiCaThi.DANG_DIEN_RA);
        caThi.setCapNhatLuc(Instant.now());
        return caThiRepository.save(caThi);
    }

    /**
     * Kết thúc ca thi — chuyển trạng thái về DA_KET_THUC.
     */
    @Transactional
    public CaThi ketThucCaThi(Integer ma) {
        CaThi caThi = caThiRepository.findById(ma)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca thi với mã: " + ma));
        if (caThi.getTrangThai() != TrangThaiCaThi.DANG_DIEN_RA) {
            throw new IllegalStateException(
                    "Chỉ có thể kết thúc ca thi đang diễn ra. Trạng thái hiện tại: "
                    + caThi.getTrangThai());
        }
        caThi.setTrangThai(TrangThaiCaThi.DA_KET_THUC);
        caThi.setCapNhatLuc(Instant.now());
        return caThiRepository.save(caThi);
    }

    /**
     * Kết thúc đồng thời mọi ca đang ở trạng thái {@link TrangThaiCaThi#DANG_DIEN_RA}
     * (dùng cho thao tác khẩn cấp trên bảng điều khiển quản trị).
     *
     * @return số ca đã chuyển sang {@link TrangThaiCaThi#DA_KET_THUC}
     */
    @Transactional(readOnly = false)
    public int ketThucTatCaDangDienRa() {
        List<CaThi> dangDienRa = caThiRepository.findByTrangThai(TrangThaiCaThi.DANG_DIEN_RA);
        if (dangDienRa.isEmpty()) {
            return 0;
        }
        Instant now = Instant.now();
        for (CaThi caThi : dangDienRa) {
            caThi.setTrangThai(TrangThaiCaThi.DA_KET_THUC);
            caThi.setCapNhatLuc(now);
        }
        caThiRepository.saveAll(dangDienRa);
        return dangDienRa.size();
    }

    // ── Xóa ───────────────────────────────────────────────────────────────────

    @Transactional
    public void xoaCaThi(Integer ma) {
        caThiRepository.deleteById(ma);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Xây dựng CaThiFormDTO từ entity để điền sẵn dữ liệu vào form chỉnh sửa.
     */
    public CaThiFormDTO toFormDTO(CaThi caThi) {
        return CaThiFormDTO.builder()
                .maDeThi(caThi.getDeThi() != null ? caThi.getDeThi().getMa() : null)
                .maKhoa(caThi.getKhoa() != null ? caThi.getKhoa().getMa() : null)
                .tenCaThi(caThi.getTenCaThi())
                .thoiGianBatDau(toLocalDateTime(caThi.getThoiGianBatDau()))
                .thoiGianKetThuc(toLocalDateTime(caThi.getThoiGianKetThuc()))
                .diaDiem(caThi.getDiaDiem())
                .soLuongToiDa(caThi.getSoLuongToiDa())
                .trangThai(caThi.getTrangThai())
                .build();
    }

    /** Chuyển Instant (UTC) → LocalDateTime (Asia/Ho_Chi_Minh) để hiển thị trong form. */
    public LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZONE_VN).toLocalDateTime();
    }

    /** Chuyển LocalDateTime (Asia/Ho_Chi_Minh) → Instant (UTC) để lưu vào DB. */
    private Instant toInstant(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atZone(ZONE_VN).toInstant();
    }

    private List<String> docMssvTuExcel(MultipartFile fileSinhVien) {
        if (fileSinhVien == null || fileSinhVien.isEmpty()) {
            return List.of();
        }

        String tenFile = fileSinhVien.getOriginalFilename();
        if (tenFile == null || (!tenFile.endsWith(".xlsx") && !tenFile.endsWith(".xls"))) {
            throw new IllegalArgumentException("File danh sách sinh viên phải có định dạng .xlsx hoặc .xls");
        }

        Set<String> uniqueMssv = new LinkedHashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = fileSinhVien.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("File Excel không có dữ liệu");
            }

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();
            for (int i = firstRow; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                String mssv = formatter.formatCellValue(cell).trim();
                if (mssv.isEmpty()) {
                    continue;
                }
                if (i == firstRow && "mssv".equalsIgnoreCase(mssv)) {
                    continue;
                }
                uniqueMssv.add(mssv);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc file Excel danh sách sinh viên", e);
        }

        if (uniqueMssv.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy MSSV hợp lệ trong file Excel");
        }

        return new ArrayList<>(uniqueMssv);
    }

    private void themDanhSachSinhVien(CaThi caThi, List<String> danhSachMssv, boolean boQuaNeuDaTonTai) {
        for (int i = 0; i < danhSachMssv.size(); i++) {
            String mssv = danhSachMssv.get(i);
            int dongExcel = i + 1;
            SinhVien sinhVien = sinhVienRepository.findByMaSinhVien(mssv)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy sinh viên có MSSV: " + mssv + " (dòng " + dongExcel + " trong file)"));

            boolean daTonTai = dangKyThiRepository.findByCaThiMaAndSinhVienMa(caThi.getMa(), sinhVien.getMa())
                    .isPresent();
            if (daTonTai) {
                if (boQuaNeuDaTonTai) {
                    continue;
                }
                throw new IllegalArgumentException("Sinh viên " + mssv + " đã tồn tại trong danh sách ca thi");
            }

            DangKyThi dangKy = DangKyThi.builder()
                    .caThi(caThi)
                    .sinhVien(sinhVien)
                    .trangThaiDangKy(TrangThaiDangKy.DA_XAC_NHAN)
                    .dangKyLuc(Instant.now())
                    .daCheckIn(false)
                    .build();
            dangKyThiRepository.save(dangKy);
        }
    }
}
