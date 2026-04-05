package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.KhoaDTO;
import com.example.server.service.KhoaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khoa")
@RequiredArgsConstructor
public class KhoaController {

    private final KhoaService khoaService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachKhoa(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "ma"));

        org.springframework.data.domain.Page<KhoaDTO> khoaPage =
                khoaService.layDanhSachPhanTrang(keyword, pageable);

        List<Map<String, Object>> danhSach = khoaPage.getContent().stream()
                .map(this::chuyenDoiKhoa)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", khoaPage.getNumber());
        response.put("tongTrang", khoaPage.getTotalPages());
        response.put("tongPhanTu", khoaPage.getTotalElements());
        response.put("kichThuocTrang", khoaPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietKhoa(@PathVariable Integer ma) {
        KhoaDTO dto = khoaService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiKhoa(dto)));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaKhoa() {
        List<Map<String, Object>> danhSach = khoaService.layTatCa().stream()
                .map(this::chuyenDoiKhoa)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    @GetMapping("/theo-truong/{maTruong}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layKhoaTheoTruong(
            @PathVariable Integer maTruong) {
        List<Map<String, Object>> danhSach = khoaService.layTheoTruong(maTruong).stream()
                .map(this::chuyenDoiKhoa)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoKhoa(
            @Valid @RequestBody KhoaDTO dto,
            HttpServletRequest request) {
        KhoaDTO created = khoaService.tao(dto, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm khoa thành công", chuyenDoiKhoa(created)));
    }

    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatKhoa(
            @PathVariable Integer ma,
            @Valid @RequestBody KhoaDTO dto,
            HttpServletRequest request) {
        KhoaDTO updated = khoaService.capNhat(ma, dto, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoa thành công", chuyenDoiKhoa(updated)));
    }

    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaKhoa(
            @PathVariable Integer ma,
            HttpServletRequest request) {
        khoaService.xoa(ma, request);
        return ResponseEntity.ok(ApiResponse.success("Xóa khoa thành công", null));
    }

    private Map<String, Object> chuyenDoiKhoa(KhoaDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("ten", dto.getTen());
        map.put("maTruong", dto.getMaTruong());
        return map;
    }
}
