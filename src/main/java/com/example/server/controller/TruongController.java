package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.TruongDTO;
import com.example.server.service.TruongService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/truong")
@RequiredArgsConstructor
public class TruongController {

    private final TruongService truongService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachTruong(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "ma"));

        org.springframework.data.domain.Page<TruongDTO> truongPage =
                truongService.layDanhSachPhanTrang(keyword, pageable);

        List<Map<String, Object>> danhSach = truongPage.getContent().stream()
                .map(this::chuyenDoiTruong)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", truongPage.getNumber());
        response.put("tongTrang", truongPage.getTotalPages());
        response.put("tongPhanTu", truongPage.getTotalElements());
        response.put("kichThuocTrang", truongPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietTruong(@PathVariable Integer ma) {
        TruongDTO dto = truongService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiTruong(dto)));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaTruong() {
        List<Map<String, Object>> danhSach = truongService.layTatCa().stream()
                .map(this::chuyenDoiTruong)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoTruong(
            @Valid @RequestBody TruongDTO dto,
            HttpServletRequest request) {
        TruongDTO created = truongService.tao(dto, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm trường thành công", chuyenDoiTruong(created)));
    }

    @PostMapping("/kem-khoa")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> taoTruongKemKhoa(
            @Valid @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        TruongDTO truongDTO = new TruongDTO();
        truongDTO.setTen((String) body.get("ten"));
        truongDTO.setCapBac(body.get("capBac") != null
                ? com.example.server.model.enums.CapBacTruong.valueOf((String) body.get("capBac"))
                : null);
        truongDTO.setMaDinhDanh((String) body.get("maDinhDanh"));
        truongDTO.setDiaChi((String) body.get("diaChi"));

        @SuppressWarnings("unchecked")
        List<String> tenKhoas = (List<String>) body.get("tenKhoas");

        List<TruongDTO> created = truongService.taoKemNhieuKhoa(truongDTO, tenKhoas, request);
        List<Map<String, Object>> result = created.stream()
                .map(this::chuyenDoiTruong)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Thêm trường và khoa thành công", result));
    }

    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatTruong(
            @PathVariable Integer ma,
            @Valid @RequestBody TruongDTO dto,
            HttpServletRequest request) {
        TruongDTO updated = truongService.capNhat(ma, dto, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trường thành công", chuyenDoiTruong(updated)));
    }

    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaTruong(
            @PathVariable Integer ma,
            HttpServletRequest request) {
        truongService.xoa(ma, request);
        return ResponseEntity.ok(ApiResponse.success("Xóa trường thành công", null));
    }

    private Map<String, Object> chuyenDoiTruong(TruongDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("ten", dto.getTen());
        map.put("capBac", dto.getCapBac() != null ? dto.getCapBac().name() : null);
        map.put("maDinhDanh", dto.getMaDinhDanh());
        map.put("diaChi", dto.getDiaChi());
        return map;
    }
}
