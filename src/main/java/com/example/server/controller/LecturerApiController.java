package com.example.server.controller;

import com.example.server.dto.ChiTietKhoaHocVm;
import com.example.server.dto.DanhSachSinhVienVm;
import com.example.server.dto.KhoaHocCuaToiVm;
import com.example.server.service.ChiTietKhoaHocService;
import com.example.server.service.DanhSachSinhVienService;
import com.example.server.service.KhoaHocCuaToiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller cho các chức năng của Giảng Viên.
 */
@RestController
@RequestMapping("/api/lecturer")
@CrossOrigin(origins = "*")
public class LecturerApiController {

    private final KhoaHocCuaToiService khoaHocCuaToiService;
    private final ChiTietKhoaHocService chiTietKhoaHocService;
    private final DanhSachSinhVienService danhSachSinhVienService;

    public LecturerApiController(
            KhoaHocCuaToiService khoaHocCuaToiService,
            ChiTietKhoaHocService chiTietKhoaHocService,
            DanhSachSinhVienService danhSachSinhVienService) {
        this.khoaHocCuaToiService = khoaHocCuaToiService;
        this.chiTietKhoaHocService = chiTietKhoaHocService;
        this.danhSachSinhVienService = danhSachSinhVienService;
    }

    /**
     * API lấy danh sách khóa học của giảng viên.
     * Không truyền maGiangVien → lấy theo tài khoản đang đăng nhập.
     *
     * GET /api/lecturer/khoahoccoatoi
     * GET /api/lecturer/khoahoccoatoi?maGiangVien=1
     */
    @GetMapping("/khoahoccoatoi")
    public ResponseEntity<List<KhoaHocCuaToiVm>> getKhoaHocCuaToi(
            @RequestParam(required = false) Integer maGiangVien) {
        Integer ma = maGiangVien != null
                ? maGiangVien
                : khoaHocCuaToiService.layMaGiangVienDangNhap().orElse(null);
        if (ma == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(khoaHocCuaToiService.layDanhSachKhoaHocCuaGiangVien(ma));
    }

    /**
     * API lấy chi tiết khóa học theo mã Giảng Viên - Môn Học.
     *
     * GET /api/lecturer/chitietkhoahoc?maGiangVienMonHoc=1
     */
    @GetMapping("/chitietkhoahoc")
    public ResponseEntity<ChiTietKhoaHocVm> getChiTietKhoaHoc(
            @RequestParam Integer maGiangVienMonHoc) {
        ChiTietKhoaHocVm chiTiet = chiTietKhoaHocService.layChiTietKhoaHoc(maGiangVienMonHoc);
        if (chiTiet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chiTiet);
    }

    /**
     * API lấy danh sách sinh viên theo môn học.
     *
     * GET /api/lecturer/danhsachsinhvien?maGiangVienMonHoc=1
     */
    @GetMapping("/danhsachsinhvien")
    public ResponseEntity<DanhSachSinhVienVm> getDanhSachSinhVien(
            @RequestParam Integer maGiangVienMonHoc) {
        DanhSachSinhVienVm danhSach = danhSachSinhVienService.layDanhSachSinhVien(maGiangVienMonHoc);
        if (danhSach == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(danhSach);
    }

}
