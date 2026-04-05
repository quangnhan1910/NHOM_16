package com.example.server.controller;

import com.example.server.model.DangKyThi;
import com.example.server.service.ThaoTacCaThiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller xử lý trang "Thao tác ca thi" – giao diện dành cho giám thị/giám khảo.
 * URL cơ sở: /admin/ca-thi/{ma}/thao-tac
 */
@Controller
@RequestMapping("/admin/ca-thi/{ma}/thao-tac")
@RequiredArgsConstructor
public class ThaoTacCaThiController {

    private final ThaoTacCaThiService thaoTacCaThiService;

    // ── Trang chính ───────────────────────────────────────────────────────────

    /**
     * Hiển thị danh sách dự thi với lọc check-in và tìm kiếm.
     *
     * @param daCheckIn null = tất cả, true = đã check-in, false = chưa check-in
     * @param tuKhoa    tìm theo MSSV hoặc họ tên
     */
    @GetMapping
    public String trangThaoTac(
            @PathVariable Integer ma,
            @RequestParam(required = false) Boolean daCheckIn,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes) {

        return thaoTacCaThiService.getCaThiByMa(ma).map(caThi -> {

            Page<DangKyThi> pageDangKy =
                    thaoTacCaThiService.getDanhSachDangKy(ma, daCheckIn, tuKhoa, page);
            ThaoTacCaThiService.ThongKe thongKe =
                    thaoTacCaThiService.getThongKe(ma, caThi);

            long total    = pageDangKy.getTotalElements();
            int fromIndex = total == 0 ? 0 : (int) pageDangKy.getPageable().getOffset() + 1;
            int toIndex   = (int) Math.min(
                    pageDangKy.getPageable().getOffset() + pageDangKy.getSize(), total);

            model.addAttribute("caThi",           caThi);
            model.addAttribute("dangKyThis",       pageDangKy.getContent());
            model.addAttribute("thongKe",          thongKe);
            model.addAttribute("daCheckInFilter",  daCheckIn);
            model.addAttribute("tuKhoa",           tuKhoa != null ? tuKhoa : "");
            model.addAttribute("currentPage",      page);
            model.addAttribute("totalPages",       pageDangKy.getTotalPages());
            model.addAttribute("totalElements",    total);
            model.addAttribute("fromIndex",        fromIndex);
            model.addAttribute("toIndex",          toIndex);

            return "admin/ca-thi/thao-tac";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không tìm thấy ca thi #" + ma);
            return "redirect:/admin/ca-thi";
        });
    }

    // ── Check-in ──────────────────────────────────────────────────────────────

    @PostMapping("/dang-ky/{maDangKy}/check-in")
    public String toggleCheckIn(
            @PathVariable Integer ma,
            @PathVariable Integer maDangKy,
            @RequestParam(required = false) Boolean daCheckIn,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            boolean newState = thaoTacCaThiService.toggleCheckIn(maDangKy);
            redirectAttributes.addFlashAttribute("successMessage",
                    newState ? "Check-in thành công!" : "Đã hủy check-in.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi check-in: " + e.getMessage());
        }
        return buildRedirect(ma, daCheckIn, tuKhoa, page);
    }

    // ── Xóa sinh viên khỏi danh sách ──────────────────────────────────────────

    @PostMapping("/dang-ky/{maDangKy}/xoa")
    public String xoaDangKy(
            @PathVariable Integer ma,
            @PathVariable Integer maDangKy,
            @RequestParam(required = false) Boolean daCheckIn,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            thaoTacCaThiService.xoaDangKy(maDangKy);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa sinh viên khỏi danh sách dự thi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa: " + e.getMessage());
        }
        return buildRedirect(ma, daCheckIn, tuKhoa, page);
    }

    // ── Thêm sinh viên ────────────────────────────────────────────────────────

    @PostMapping("/them-sinh-vien")
    public String themSinhVien(
            @PathVariable Integer ma,
            @RequestParam String maSinhVien,
            RedirectAttributes redirectAttributes) {
        try {
            thaoTacCaThiService.themSinhVien(ma, maSinhVien);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm sinh viên " + maSinhVien.trim() + " vào danh sách dự thi thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ca-thi/" + ma + "/thao-tac";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Xây dựng URL redirect về trang thao tác, giữ nguyên bộ lọc và trang hiện tại. */
    private String buildRedirect(Integer ma, Boolean daCheckIn, String tuKhoa, int page) {
        StringBuilder sb = new StringBuilder("redirect:/admin/ca-thi/")
                .append(ma).append("/thao-tac?page=").append(page);
        if (daCheckIn != null) {
            sb.append("&daCheckIn=").append(daCheckIn);
        }
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sb.append("&tuKhoa=")
              .append(URLEncoder.encode(tuKhoa.trim(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
