package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.ChuyenNganhDTO;
import com.example.server.service.ChuyenNganhService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chuyen-nganh")
@RequiredArgsConstructor
public class ChuyenNganhController {

    private final ChuyenNganhService chuyenNganhService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachChuyenNganh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer maNganh) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "ma"));

        org.springframework.data.domain.Page<ChuyenNganhDTO> cnPage =
                chuyenNganhService.layDanhSachPhanTrang(keyword, maNganh, pageable);

        List<Map<String, Object>> danhSach = cnPage.getContent().stream()
                .map(this::chuyenDoiChuyenNganh)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", cnPage.getNumber());
        response.put("tongTrang", cnPage.getTotalPages());
        response.put("tongPhanTu", cnPage.getTotalElements());
        response.put("kichThuocTrang", cnPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietChuyenNganh(@PathVariable Integer ma) {
        ChuyenNganhDTO dto = chuyenNganhService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiChuyenNganh(dto)));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaChuyenNganh() {
        List<Map<String, Object>> danhSach = chuyenNganhService.layTatCa().stream()
                .map(this::chuyenDoiChuyenNganh)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    @GetMapping("/theo-nganh/{maNganh}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layChuyenNganhTheoNganh(@PathVariable Integer maNganh) {
        List<Map<String, Object>> danhSach = chuyenNganhService.layTheoNganh(maNganh).stream()
                .map(this::chuyenDoiChuyenNganh)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoChuyenNganh(
            @Valid @RequestBody ChuyenNganhDTO dto,
            HttpServletRequest request) {
        ChuyenNganhDTO created = chuyenNganhService.tao(dto, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm chuyên ngành thành công", chuyenDoiChuyenNganh(created)));
    }

    @PostMapping("/theo-nganh")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> taoNhieuChuyenNganh(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Integer maNganh = body.get("maNganh") != null
                ? ((Number) body.get("maNganh")).intValue()
                : null;

        @SuppressWarnings("unchecked")
        List<String> tenChuyenNganhs = (List<String>) body.get("tenChuyenNganhs");

        List<ChuyenNganhDTO> created = chuyenNganhService.taoKemNhieuChuyenNganh(maNganh, tenChuyenNganhs, request);
        List<Map<String, Object>> result = created.stream()
                .map(dto -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ma", dto.getMa());
                    m.put("ten", dto.getTen());
                    m.put("maNganh", dto.getMaNganh());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Thêm chuyên ngành thành công", result));
    }

    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatChuyenNganh(
            @PathVariable Integer ma,
            @Valid @RequestBody ChuyenNganhDTO dto,
            HttpServletRequest request) {
        ChuyenNganhDTO updated = chuyenNganhService.capNhat(ma, dto, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chuyên ngành thành công", chuyenDoiChuyenNganh(updated)));
    }

    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaChuyenNganh(
            @PathVariable Integer ma,
            HttpServletRequest request) {
        chuyenNganhService.xoa(ma, request);
        return ResponseEntity.ok(ApiResponse.success("Xóa chuyên ngành thành công", null));
    }

    private Map<String, Object> chuyenDoiChuyenNganh(ChuyenNganhDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("ten", dto.getTen());
        map.put("maNganh", dto.getMaNganh());
        return map;
    }
}
