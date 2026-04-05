package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.model.enums.VaiTro;
import com.example.server.service.NguoiDungService;
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
 * API quản lý người dùng.
 * Phạm vi: QUAN_TRI_VIEN (CRUD đầy đủ), GIANG_VIEN (chỉ xem chi tiết của mình).
 */
@RestController
@RequestMapping("/api/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    /**
     * GET /api/nguoi-dung
     * Danh sách phân trang với filter.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> layDanhSachNguoiDung(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VaiTro vaiTro,
            @RequestParam(required = false) Boolean trangThai) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ma"));
        Page<NguoiDungDTO> nguoiDungPage = nguoiDungService.layDanhSachPhanTrang(keyword, vaiTro, trangThai, pageable);

        List<Map<String, Object>> danhSach = nguoiDungPage.getContent().stream()
                .map(this::chuyenDoiNguoiDung)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("danhSach", danhSach);
        response.put("trangHienTai", nguoiDungPage.getNumber());
        response.put("tongTrang", nguoiDungPage.getTotalPages());
        response.put("tongPhanTu", nguoiDungPage.getTotalElements());
        response.put("kichThuocTrang", nguoiDungPage.getSize());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/nguoi-dung/{ma}
     * Lấy chi tiết người dùng theo ID.
     */
    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layChiTietNguoiDung(@PathVariable Integer ma) {
        NguoiDungDTO dto = nguoiDungService.timTheoMa(ma);
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiNguoiDung(dto)));
    }

    /**
     * GET /api/nguoi-dung/current
     * Lấy thông tin người dùng hiện tại đang đăng nhập.
     * Dùng cho cai-dat-admin.html.
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> layNguoiDungHienTai() {
        NguoiDungDTO dto = nguoiDungService.layNguoiDungHienTai();
        return ResponseEntity.ok(ApiResponse.success(chuyenDoiNguoiDung(dto)));
    }

    /**
     * PUT /api/nguoi-dung/doi-mat-khau
     * Đổi mật khẩu cho người dùng hiện tại.
     * Body: { matKhauHienTai, matKhauMoi, xacNhanMatKhauMoi }
     */
    @PutMapping("/doi-mat-khau")
    public ResponseEntity<ApiResponse<Void>> doiMatKhau(
            @Valid @RequestBody DoiMatKhauRequest request,
            HttpServletRequest httpRequest) {
        nguoiDungService.doiMatKhau(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công.", null));
    }

    /**
     * GET /api/nguoi-dung/tat-ca
     * Danh sách tất cả người dùng (không phân trang).
     * Dùng cho dropdown filter.
     */
    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> layTatCaNguoiDung() {
        List<Map<String, Object>> danhSach = nguoiDungService.layTatCa().stream()
                .map(this::chuyenDoiNguoiDung)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(danhSach));
    }

    /**
     * GET /api/nguoi-dung/thong-ke
     * Thống kê người dùng.
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<ApiResponse<ThongKeNguoiDungDTO>> thongKeNguoiDung() {
        ThongKeNguoiDungDTO thongKe = nguoiDungService.thongKe();
        return ResponseEntity.ok(ApiResponse.success(thongKe));
    }

    /**
     * POST /api/nguoi-dung
     * Tạo mới người dùng.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> taoNguoiDung(
            @Valid @RequestBody TaoNguoiDungRequest request,
            HttpServletRequest httpRequest) {
        NguoiDungDTO created = nguoiDungService.tao(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Thêm người dùng thành công", chuyenDoiNguoiDung(created)));
    }

    /**
     * PUT /api/nguoi-dung/{ma}
     * Cập nhật người dùng.
     */
    @PutMapping("/{ma}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capNhatNguoiDung(
            @PathVariable Integer ma,
            @Valid @RequestBody CapNhatNguoiDungRequest request,
            HttpServletRequest httpRequest) {
        NguoiDungDTO updated = nguoiDungService.capNhat(ma, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", chuyenDoiNguoiDung(updated)));
    }

    /**
     * PUT /api/nguoi-dung/{ma}/khoa
     * Khóa tài khoản người dùng.
     */
    @PutMapping("/{ma}/khoa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> khoaNguoiDung(
            @PathVariable Integer ma,
            HttpServletRequest httpRequest) {
        NguoiDungDTO updated = nguoiDungService.khoa(ma, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Khóa tài khoản thành công", chuyenDoiNguoiDung(updated)));
    }

    /**
     * PUT /api/nguoi-dung/{ma}/mo
     * Mở khóa tài khoản người dùng.
     */
    @PutMapping("/{ma}/mo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> moKhoaNguoiDung(
            @PathVariable Integer ma,
            HttpServletRequest httpRequest) {
        NguoiDungDTO updated = nguoiDungService.moKhoa(ma, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản thành công", chuyenDoiNguoiDung(updated)));
    }

    /**
     * DELETE /api/nguoi-dung/{ma}
     * Xóa người dùng — chỉ xóa thực sự nếu không có ràng buộc nghiệp vụ.
     * Nếu có ràng buộc, tự động khóa thay vì xóa.
     */
    @DeleteMapping("/{ma}")
    public ResponseEntity<ApiResponse<Void>> xoaNguoiDung(
            @PathVariable Integer ma,
            HttpServletRequest httpRequest) {
        nguoiDungService.xoa(ma, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }

    /**
     * Chuyển đổi DTO sang Map.
     */
    private Map<String, Object> chuyenDoiNguoiDung(NguoiDungDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("ma", dto.getMa());
        map.put("thuDienTu", dto.getThuDienTu());
        map.put("hoTen", dto.getHoTen());
        map.put("vaiTro", dto.getVaiTro());
        map.put("trangThaiHoatDong", dto.getTrangThaiHoatDong());
        map.put("taoLuc", dto.getTaoLuc());
        map.put("capNhatLuc", dto.getCapNhatLuc());
        if (dto.getMaTruong() != null) map.put("maTruong", dto.getMaTruong());
        if (dto.getMaKhoa() != null) map.put("maKhoa", dto.getMaKhoa());
        if (dto.getMaNhanVien() != null) map.put("maNhanVien", dto.getMaNhanVien());
        if (dto.getMaChuyenNganh() != null) map.put("maChuyenNganh", dto.getMaChuyenNganh());
        if (dto.getMaSinhVien() != null) map.put("maSinhVien", dto.getMaSinhVien());
        if (dto.getBacDaoTao() != null) map.put("bacDaoTao", dto.getBacDaoTao());
        return map;
    }
}
