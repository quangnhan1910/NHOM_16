package com.example.server.controller;

/*
    =================================================================
    File: LecturerController.java
    Mô tả: Controller xử lý các request liên quan đến trang Giang Vien
    Framework: Spring Boot MVC
    =================================================================
*/

import com.example.server.dto.ChiTietKhoaHocVm;
import com.example.server.dto.DeThiTheoLoaiBar;
import com.example.server.dto.HoatDongGanDayVm;
import com.example.server.dto.KhoaHocCuaToiVm;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.service.BangDieuKhienGiangVienService;
import com.example.server.service.ChiTietKhoaHocService;
import com.example.server.service.KhoaHocCuaToiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Lecturer Bang Dieu Khien Gian Vien views.
 *
 * Chức năng:
 * - Xử lý các request liên quan đến trang bảng điều khiển của giảng viên
 * - Trả về các view template Thymeleaf
 * 
 * Các endpoint:
 * - GET /lecturer/bang-dieu-khien-giang-vien: Hiển thị trang bang dieu khien giang vien
 * - GET /lecturer/khoahoccoatoi: Trang Khóa học của tôi (Thymeleaf)
 */
@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    private final BangDieuKhienGiangVienService bangDieuKhienGiangVienService;
    private final KhoaHocCuaToiService khoaHocCuaToiService;
    private final ChiTietKhoaHocService chiTietKhoaHocService;

    public LecturerController(
            BangDieuKhienGiangVienService bangDieuKhienGiangVienService,
            KhoaHocCuaToiService khoaHocCuaToiService,
            ChiTietKhoaHocService chiTietKhoaHocService) {
        this.bangDieuKhienGiangVienService = bangDieuKhienGiangVienService;
        this.khoaHocCuaToiService = khoaHocCuaToiService;
        this.chiTietKhoaHocService = chiTietKhoaHocService;
    }

    /**
     * Hiển thị trang Bang dieu khien (Giang Vien) dành cho Giang Vien.
     * 
     * Mapping: GET /lecturer/bang-dieu-khien-giang-vien
     * 
     * Workflow:
     * 1. User truy cập URL: http://localhost:8080/lecturer/bang-dieu-khien-giang-vien
     * 2. Spring MVC nhận request và gọi phương thức bangDieuKhienGiangVien()
     * 3. Phương thức trả về tên view "lecturer/bang-dieu-khien-giang-vien"
     * 4. Thymeleaf sẽ render file: src/main/resources/templates/lecturer/bang-dieu-khien-giang-vien.html
     * 
     * @return Tên view template (không có extension .html)
     */
    @GetMapping("/bang-dieu-khien-giang-vien")
    public String bangDieuKhienGiangVien(Model model) {
        var svc = bangDieuKhienGiangVienService;
        long tongCa = svc.demTongCaThi();
        long tongCau = svc.demTongCauHoi();
        long luot = svc.demLuotLamBai();
        long daKetThuc = svc.demCaTheoTrangThai(TrangThaiCaThi.DA_KET_THUC);
        long dangDienRa = svc.demCaTheoTrangThai(TrangThaiCaThi.DANG_DIEN_RA);
        long choDangKy = svc.demCaTheoTrangThai(TrangThaiCaThi.CHO_DANG_KY);
        long tongTk = svc.tongCaChoThongKe();

        model.addAttribute("tongCaThi", tongCa);
        model.addAttribute("tongCauHoi", tongCau);
        model.addAttribute("luotLamBai", luot);
        model.addAttribute("caDaKetThuc", daKetThuc);
        model.addAttribute("caDangDienRa", dangDienRa);
        model.addAttribute("caChoDangKy", choDangKy);
        model.addAttribute("pctKetThuc", svc.phanTram(daKetThuc, tongTk));
        model.addAttribute("pctDang", svc.phanTram(dangDienRa, tongTk));
        model.addAttribute("pctCho", svc.phanTram(choDangKy, tongTk));
        var deTheoLoai = svc.deThiTheoLoai();
        model.addAttribute("deThiTheoLoai", deTheoLoai);
        model.addAttribute("coDuLieuBieuDoDe", deTheoLoai.stream().mapToLong(DeThiTheoLoaiBar::soLuong).sum() > 0);
        List<HoatDongGanDayVm> nhatKy = svc.hoatDongGanDay();
        model.addAttribute("hoatDongGanDay", nhatKy);
        model.addAttribute("coHoatDong", !nhatKy.isEmpty());
        model.addAttribute("coDuLieuTrangThaiCa", tongTk > 0);
        return "lecturer/bang-dieu-khien-giang-vien";
    }

    /**
     * Trang Khóa học của tôi — GET /lecturer/khoahoccoatoi
     * Dữ liệu: server render theo giảng viên đang đăng nhập; API: GET /api/lecturer/khoahoccoatoi
     */
    @GetMapping("/khoahoccoatoi")
    public String khoaHocCuaToi(Model model) {
        Optional<Integer> maGv = khoaHocCuaToiService.layMaGiangVienDangNhap();
        List<KhoaHocCuaToiVm> danhSach = maGv
                .map(khoaHocCuaToiService::layDanhSachKhoaHocCuaGiangVien)
                .orElse(List.of());
        model.addAttribute("danhSachKhoaHoc", danhSach);
        model.addAttribute("coKhoaHoc", !danhSach.isEmpty());
        // true khi tài khoản đăng nhập không có dòng giang_vien (vd. quản trị viên mở URL giảng viên)
        model.addAttribute("taiKhoanKhongPhaiGiangVien", maGv.isEmpty());
        return "lecturer/khoahoccoatoi";
    }

    /**
     * Trang Chi tiết khóa học — GET /lecturer/chitietkhoahoc?maGiangVienMonHoc=1
     */
    @GetMapping("/chitietkhoahoc")
    public String chiTietKhoaHoc(@RequestParam Integer maGiangVienMonHoc, Model model) {
        ChiTietKhoaHocVm chiTiet = chiTietKhoaHocService.layChiTietKhoaHoc(maGiangVienMonHoc);
        if (chiTiet == null) {
            return "redirect:/lecturer/khoahoccoatoi";
        }
        model.addAttribute("chiTietKhoaHoc", chiTiet);
        model.addAttribute("maGiangVienMonHoc", maGiangVienMonHoc);
        model.addAttribute("hienNutSuaPhanCongAdmin", laQuanTriVienDangNhap());
        return "lecturer/chi-tiet-khoa-hoc";
    }

    private static boolean laQuanTriVienDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_QUAN_TRI_VIEN".equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
