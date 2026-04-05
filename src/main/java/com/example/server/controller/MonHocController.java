package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.MonHocDTO;
import com.example.server.service.MonHocService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API quản lý môn học.
 * Dùng chung cho QUAN_TRI_VIEN và GIANG_VIEN (chỉ đọc).
 */
@RestController
@RequestMapping("/api/mon-hoc")
@RequiredArgsConstructor
public class MonHocController {

    private final MonHocService monHocService;

    /**
     * GET /api/mon-hoc
     * Danh sách phân trang với filter.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachMonHoc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer maKhoa,
            @RequestParam(required = false) Integer soTinChi) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ma"));
        Page<MonHocDTO> monHocPage = monHocService.layDanhSachPhanTrang(keyword, maKhoa, soTinChi, pageable);

        List<Map<String, Object>> danhSach = monHocPage.getContent().stream()
                .map(this::chuyenDoiMonHoc)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", monHocPage.getNumber());
        response.put("tongTrang", monHocPage.getTotalPages());
        response.put("tongPhanTu", monHocPage.getTotalElements());
        response.put("kichThuocTrang", monHocPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/mon-hoc/{ma}
     * Lấy chi tiết môn học.
     */
    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietMonHoc(@PathVariable Integer ma) {
        MonHocDTO dto = monHocService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiMonHoc(dto)));
    }

    /**
     * GET /api/mon-hoc/tat-ca
     * Danh sách tất cả môn học (không phân trang).
     * Dùng cho dropdown filter.
     */
    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaMonHoc() {
        List<Map<String, Object>> danhSach = monHocService.layTatCa().stream()
                .map(this::chuyenDoiMonHoc)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    /**
     * POST /api/mon-hoc
     * Tạo mới môn học.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoMonHoc(
            @Valid @RequestBody MonHocDTO dto,
            HttpServletRequest httpRequest) {
        MonHocDTO created = monHocService.tao(dto, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Thêm môn học thành công", chuyenDoiMonHoc(created)));
    }

    /**
     * PUT /api/mon-hoc/{ma}
     * Cập nhật môn học.
     */
    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatMonHoc(
            @PathVariable Integer ma,
            @Valid @RequestBody MonHocDTO dto,
            HttpServletRequest httpRequest) {
        MonHocDTO updated = monHocService.capNhat(ma, dto, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn học thành công", chuyenDoiMonHoc(updated)));
    }

    /**
     * DELETE /api/mon-hoc/{ma}
     * Xóa môn học — chỉ xóa được nếu không có ràng buộc đề thi/câu hỏi/giảng viên.
     */
    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaMonHoc(
            @PathVariable Integer ma,
            HttpServletRequest httpRequest) {
        monHocService.xoa(ma, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Xóa môn học thành công.", null));
    }

    /**
     * Chuyển đổi MonHocDTO sang Map.
     */
    private Map<String, Object> chuyenDoiMonHoc(MonHocDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("ten", dto.getTen());
        map.put("maDinhDanh", dto.getMaDinhDanh());
        map.put("soTinChi", dto.getSoTinChi());
        map.put("maKhoa", dto.getMaKhoa());
        map.put("tenKhoa", dto.getTenKhoa());
        map.put("taoLuc", dto.getTaoLuc());
        map.put("capNhatLuc", dto.getCapNhatLuc());
        return map;
    }
}
