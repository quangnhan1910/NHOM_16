package com.example.server.controller;

import com.example.server.dto.ApiResponse;
import com.example.server.model.NhatKyHeThong;
import com.example.server.repository.NhatKyHeThongRepository;
import com.example.server.service.HanhDong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API quản lý nhật ký hệ thống.
 */
@RestController
@RequestMapping("/api/nhat-ky-he-thong")
@RequiredArgsConstructor
public class NhatKyHeThongController {

    private final NhatKyHeThongRepository nhatKyHeThongRepository;

    /**
     * Lấy danh sách nhật ký hệ thống với phân trang và lọc.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachNhatKy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) String hanhDong,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "taoLuc"));
        
        Instant tuNgayInstant = tuNgay != null ? tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant denNgayInstant = denNgay != null ? denNgay.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        
        Page<NhatKyHeThong> nhatKyPage = nhatKyHeThongRepository
                .searchNhatKyHeThong(keyword, vaiTro, hanhDong, tuNgayInstant, denNgayInstant, pageable);
        
        List<Map<String, Object>> danhSach = nhatKyPage.getContent().stream()
                .map(this::chuyenDoiNhatKy)
                .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", nhatKyPage.getNumber());
        response.put("tongTrang", nhatKyPage.getTotalPages());
        response.put("tongPhanTu", nhatKyPage.getTotalElements());
        response.put("kichThuocTrang", nhatKyPage.getSize());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy chi tiết nhật ký theo ID.
     */
    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietNhatKy(@PathVariable Integer ma) {
        return nhatKyHeThongRepository.findById(ma)
                .map(nk -> ResponseEntity.ok(ApiResponse.success(chuyenDoiNhatKy(nk))))
                .orElse(ResponseEntity.ok(ApiResponse.error("Không tìm thấy nhật ký", 404)));
    }

    /**
     * Chuyển đổi đối tượng NhatKyHeThong sang Map.
     */
    private Map<String, Object> chuyenDoiNhatKy(NhatKyHeThong nk) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", nk.getMa());
        map.put("vaiTroNguoiThucHien", nk.getVaiTroNguoiThucHien());
        map.put("maNguoiThucHien", nk.getMaNguoiThucHien());
        map.put("hanhDong", nk.getHanhDong());
        map.put("hanhDongMoTa", layMoTaHanhDong(nk.getHanhDong()));
        map.put("bangMucTieu", nk.getBangMucTieu());
        map.put("maMucTieu", nk.getMaMucTieu());
        map.put("diaChiIp", nk.getDiaChiIp());
        map.put("taoLuc", nk.getTaoLuc());
        return map;
    }

    private String layMoTaHanhDong(String hanhDong) {
        if (hanhDong == null || hanhDong.isBlank()) return hanhDong;
        try {
            return HanhDong.valueOf(hanhDong).getMoTa();
        } catch (Exception e) {
            return hanhDong;
        }
    }
}
