package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.NganhDTO;
import com.example.server.service.NganhService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API quản lý ngành.
 */
@RestController
@RequestMapping("/api/nganh")
@RequiredArgsConstructor
public class NganhController {

    private final NganhService nganhService;

    /**
     * Lấy danh sách ngành với phân trang và lọc.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachNganh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer maKhoa) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "ma"));

        org.springframework.data.domain.Page<NganhDTO> nganhPage =
                nganhService.layDanhSachPhanTrang(keyword, maKhoa, pageable);

        List<Map<String, Object>> danhSach = nganhPage.getContent().stream()
                .map(this::chuyenDoiNganh)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", nganhPage.getNumber());
        response.put("tongTrang", nganhPage.getTotalPages());
        response.put("tongPhanTu", nganhPage.getTotalElements());
        response.put("kichThuocTrang", nganhPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy chi tiết ngành theo ID.
     */
    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietNganh(@PathVariable Integer ma) {
        NganhDTO dto = nganhService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiNganh(dto)));
    }

    /**
     * Lấy danh sách tất cả ngành (không phân trang).
     */
    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaNganh() {
        List<Map<String, Object>> danhSach = nganhService.layTatCa().stream()
                .map(this::chuyenDoiNganh)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    /**
     * Lấy ngành theo khoa.
     */
    @GetMapping("/theo-khoa/{maKhoa}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layNganhTheoKhoa(@PathVariable Integer maKhoa) {
        List<Map<String, Object>> danhSach = nganhService.layTheoKhoa(maKhoa).stream()
                .map(this::chuyenDoiNganh)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    /**
     * Tạo mới ngành.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoNganh(
            @Valid @RequestBody NganhDTO dto,
            HttpServletRequest request) {
        NganhDTO created = nganhService.tao(dto, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm ngành thành công", chuyenDoiNganh(created)));
    }

    /**
     * Tạo nhiều ngành cho 1 khoa đã có (Form 2). Body: { "maKhoa": 1, "tenNganhs": ["CNTT", "KHMT"] }
     */
    @PostMapping("/nhieu-nganh")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> taoNhieuNganh(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Integer maKhoa = body.get("maKhoa") != null
                ? ((Number) body.get("maKhoa")).intValue()
                : null;
        @SuppressWarnings("unchecked")
        List<String> tenNganhs = (List<String>) body.get("tenNganhs");
        List<NganhDTO> created = nganhService.taoNhieuNganh(maKhoa, tenNganhs, request);
        List<Map<String, Object>> result = created.stream()
                .map(dto -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ma", dto.getMa());
                    m.put("ten", dto.getTen());
                    m.put("maKhoa", dto.getMaKhoa());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Thêm ngành thành công", result));
    }

    /**
     * Cập nhật ngành.
     */
    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatNganh(
            @PathVariable Integer ma,
            @Valid @RequestBody NganhDTO dto,
            HttpServletRequest request) {
        NganhDTO updated = nganhService.capNhat(ma, dto, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngành thành công", chuyenDoiNganh(updated)));
    }

    /**
     * Xóa ngành.
     */
    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaNganh(
            @PathVariable Integer ma,
            HttpServletRequest request) {
        nganhService.xoa(ma, request);
        return ResponseEntity.ok(ApiResponse.success("Xóa ngành thành công", null));
    }

    /**
     * Chuyển đổi DTO sang Map.
     */
    private Map<String, Object> chuyenDoiNganh(NganhDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("ten", dto.getTen());
        map.put("maKhoa", dto.getMaKhoa());
        return map;
    }
}
