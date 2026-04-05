package com.example.server.controller;

import com.example.server.dto.DeThiDTO;
import com.example.server.dto.LuuDeThiRequest;
import com.example.server.service.DeThiService;
import com.example.server.service.DocParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho đề thi — trả JSON.
 */
@RestController
@RequestMapping("/api/de-thi")
@RequiredArgsConstructor
public class DeThiAPIController {

    private final DeThiService deThiService;
    private final DocParserService docParserService;

    /**
     * GET /api/de-thi/danh-sach
     * Lấy danh sách đề thi có phân trang, filter trạng thái, và tìm kiếm.
     */
    @GetMapping("/danh-sach")
    public ResponseEntity<Page<DeThiDTO>> layDanhSach(
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Boolean daXuatBan = null;
        if ("published".equalsIgnoreCase(trangThai)) {
            daXuatBan = true;
        } else if ("draft".equalsIgnoreCase(trangThai)) {
            daXuatBan = false;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "taoLuc"));
        Page<DeThiDTO> result = deThiService.danhSach(daXuatBan, tuKhoa, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/de-thi/thong-ke
     * Thống kê số lượng đề thi theo trạng thái.
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<Map<String, Long>> layThongKe() {
        return ResponseEntity.ok(deThiService.demTheoTrangThai());
    }

    /**
     * GET /api/de-thi/mon-hoc
     * Lấy danh sách tất cả môn học (cho dropdown chọn môn).
     */
    @GetMapping("/mon-hoc")
    public ResponseEntity<List<Map<String, Object>>> layDanhSachMonHoc() {
        return ResponseEntity.ok(deThiService.layDanhSachMonHocChoFormDeThi());
    }

    /**
     * POST /api/de-thi/tao-moi
     * Tạo đề thi mới: upload file + parse nội dung + lưu DB.
     */
    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoDeThi(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenDeThi") String tenDeThi,
            @RequestParam("maMonHoc") Integer maMonHoc,
            @RequestParam(value = "thoiLuongPhut", required = false) Integer thoiLuongPhut) {
        try {
            String rawText = docParserService.parse(file);
            DeThiDTO created = deThiService.taoDeThi(tenDeThi, maMonHoc, thoiLuongPhut);

            return ResponseEntity.ok(Map.of(
                    "deThi", created,
                    "rawText", rawText
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi tạo đề thi"
            ));
        }
    }

    /**
     * POST /api/de-thi/luu
     * Lưu đề thi: tạo câu hỏi vào ngân hàng + liên kết CauHoiDeThi + cập nhật tổng điểm.
     */
    @PostMapping("/luu")
    public ResponseEntity<?> luuDeThi(@RequestBody LuuDeThiRequest request) {
        try {
            DeThiDTO saved = deThiService.luuDeThi(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Lưu đề thi thành công",
                    "deThi", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi lưu đề thi"
            ));
        }
    }

    /**
     * DELETE /api/de-thi/{ma}
     * Xóa đề thi khỏi database.
     */
    @DeleteMapping("/{ma}")
    public ResponseEntity<?> xoaDeThi(@PathVariable Integer ma) {
        try {
            deThiService.xoaDeThi(ma);
            return ResponseEntity.ok(Map.of("message", "Đã xóa đề thi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi xóa đề thi"
            ));
        }
    }

    /**
     * GET /api/de-thi/{ma}/chi-tiet
     * Lấy chi tiết đề thi (câu hỏi + lựa chọn) dạng rawText để sửa.
     */
    @GetMapping("/{ma}/chi-tiet")
    public ResponseEntity<?> layChiTiet(@PathVariable Integer ma) {
        try {
            Map<String, Object> result = deThiService.layChiTiet(ma);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra"
            ));
        }
    }
}
