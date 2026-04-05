package com.example.server.controller;

import com.example.server.dto.ImportCauHoiRequest;
import com.example.server.dto.NganHangCauHoiDTO;
import com.example.server.model.MonHoc;
import com.example.server.model.enums.LoaiCauHoi;
import com.example.server.model.enums.MucDoKho;
import com.example.server.repository.MonHocRepository;
import com.example.server.service.DocParserService;
import com.example.server.service.NganHangCauHoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho ngân hàng câu hỏi — trả JSON.
 */
@RestController
@RequestMapping("/api/ngan-hang-cau-hoi")
@RequiredArgsConstructor
public class NganHangCauHoiAPIController {

    private final NganHangCauHoiService nganHangCauHoiService;
    private final MonHocRepository monHocRepository;
    private final DocParserService docParserService;

    /**
     * POST /api/ngan-hang-cau-hoi/upload-parse
     * Nhận file Word/PDF, parse nội dung thành raw text trả về cho frontend.
     */
    @PostMapping("/upload-parse")
    public ResponseEntity<Map<String, String>> uploadAndParse(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File rỗng, vui lòng chọn file hợp lệ."));
        }

        String rawText = docParserService.parse(file);
        return ResponseEntity.ok(Map.of("rawText", rawText));
    }

    /**
     * POST /api/ngan-hang-cau-hoi/import
     * Lưu hàng loạt câu hỏi đã kiểm tra vào database.
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCauHoi(
            @RequestBody ImportCauHoiRequest request) {

        int saved = nganHangCauHoiService.importCauHoi(request);
        return ResponseEntity.ok(Map.of(
                "message", "Import thành công " + saved + " câu hỏi",
                "count", saved
        ));
    }

    /**
     * GET /api/ngan-hang-cau-hoi/mon-hoc
     * Lấy danh sách tất cả môn học (dùng cho tab filter).
     */
    @GetMapping("/mon-hoc")
    public ResponseEntity<List<Map<String, Object>>> danhSachMonHoc() {
        List<MonHoc> monHocs = monHocRepository.findAll(Sort.by("ten").ascending());
        List<Map<String, Object>> result = monHocs.stream()
                .map(mh -> Map.<String, Object>of(
                        "ma", mh.getMa(),
                        "ten", mh.getTen(),
                        "maDinhDanh", mh.getMaDinhDanh() != null ? mh.getMaDinhDanh() : ""
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/ngan-hang-cau-hoi
     * Lấy danh sách câu hỏi có phân trang + filter.
     */
    @GetMapping
    public ResponseEntity<Page<NganHangCauHoiDTO>> danhSach(
            @RequestParam(required = false) Integer monHoc,
            @RequestParam(required = false) String loaiCauHoi,
            @RequestParam(required = false) String mucDoKho,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LoaiCauHoi loaiEnum = parseEnum(LoaiCauHoi.class, loaiCauHoi);
        MucDoKho mucDoEnum = parseEnum(MucDoKho.class, mucDoKho);

        Pageable pageable = PageRequest.of(page, size, Sort.by("ma").descending());

        Page<NganHangCauHoiDTO> result = nganHangCauHoiService.danhSach(
                monHoc, loaiEnum, mucDoEnum, tuKhoa, pageable
        );

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/ngan-hang-cau-hoi/{ma}
     * Lấy chi tiết 1 câu hỏi.
     */
    @GetMapping("/{ma:\\d+}")
    public ResponseEntity<NganHangCauHoiDTO> chiTiet(@PathVariable Integer ma) {
        return ResponseEntity.ok(nganHangCauHoiService.layTheoMa(ma));
    }

    /**
     * DELETE /api/ngan-hang-cau-hoi/{ma}
     * Xóa câu hỏi.
     */
    @DeleteMapping("/{ma:\\d+}")
    public ResponseEntity<Map<String, String>> xoa(@PathVariable Integer ma) {
        nganHangCauHoiService.xoa(ma);
        return ResponseEntity.ok(Map.of("message", "Xóa câu hỏi thành công"));
    }

    /**
     * Parse enum an toàn, trả null nếu giá trị rỗng hoặc không hợp lệ.
     */
    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
