package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.dto.TaoPhanCongGiangDayRequest;
import com.example.server.service.PhanCongGiangDayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API quản trị phân công giảng viên — môn học — chuyên ngành (nhóm lớp).
 */
@RestController
@RequestMapping("/api/phan-cong-giang-day")
@RequiredArgsConstructor
public class PhanCongGiangDayApiController {

    private final PhanCongGiangDayService phanCongGiangDayService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layDanhSach() {
        return ResponseEntity.ok(ApiResponse.success(phanCongGiangDayService.layTatCaPhanCong()));
    }

    @GetMapping("/giang-vien")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layGiangVien() {
        return ResponseEntity.ok(ApiResponse.success(phanCongGiangDayService.layGiangVienChoChon()));
    }

    @GetMapping("/sinh-vien")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> laySinhVien() {
        return ResponseEntity.ok(ApiResponse.success(phanCongGiangDayService.laySinhVienChoChon()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> tao(@Valid @RequestBody TaoPhanCongGiangDayRequest body) {
        try {
            Map<String, Object> row = phanCongGiangDayService.taoPhanCong(body);
            return ResponseEntity.ok(ApiResponse.success("Thêm phân công thành công.", row));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhat(
            @PathVariable Integer ma,
            @Valid @RequestBody TaoPhanCongGiangDayRequest body) {
        try {
            Map<String, Object> row = phanCongGiangDayService.capNhatPhanCong(ma, body);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật phân công thành công.", row));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoa(@PathVariable Integer ma) {
        try {
            phanCongGiangDayService.xoaPhanCong(ma);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa phân công.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }
}
