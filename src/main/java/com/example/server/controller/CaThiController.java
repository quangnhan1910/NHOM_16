package com.example.server.controller;

import com.example.server.dto.CaThiFormDTO;
import com.example.server.model.CaThi;
import com.example.server.model.DeThi;
import com.example.server.model.Khoa;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.service.CaThiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý toàn bộ trang quản lý Ca Thi (Thymeleaf MVC).
 * URL pattern: /admin/ca-thi
 */
@Controller
@RequestMapping("/admin/ca-thi")
@RequiredArgsConstructor
public class CaThiController {

    private final CaThiService caThiService;

    // ── Danh sách ─────────────────────────────────────────────────────────────

    /**
     * Hiển thị danh sách ca thi với lọc tại DB và phân trang.
     */
    @GetMapping
    public String danhSachCaThi(
            @RequestParam(name = "trangThai", required = false) TrangThaiCaThi trangThai,
            @RequestParam(name = "maKhoa",    required = false) Integer maKhoa,
            @RequestParam(name = "page",      defaultValue = "0") int page,
            Model model) {

        Page<CaThi> pageCaThi    = caThiService.getDanhSachCaThi(page, trangThai, maKhoa);
        List<CaThi> caThis       = pageCaThi.getContent();
        List<Khoa>  danhSachKhoa = caThiService.getDanhSachKhoa();
        Map<Integer, Integer> demDangKy = caThiService.getDemSoLuongDangKy(caThis);

        long totalElements = pageCaThi.getTotalElements();
        int fromIndex = totalElements == 0 ? 0 : (int) pageCaThi.getPageable().getOffset() + 1;
        int toIndex   = (int) Math.min(
                pageCaThi.getPageable().getOffset() + pageCaThi.getSize(), totalElements);

        model.addAttribute("pageCaThi",      pageCaThi);
        model.addAttribute("caThis",         caThis);
        model.addAttribute("danhSachKhoa",   danhSachKhoa);
        model.addAttribute("demDangKy",      demDangKy);
        model.addAttribute("currentPage",    page);
        model.addAttribute("totalPages",     pageCaThi.getTotalPages());
        model.addAttribute("totalElements",  totalElements);
        model.addAttribute("pageSize",       pageCaThi.getSize());
        model.addAttribute("fromIndex",      fromIndex);
        model.addAttribute("toIndex",        toIndex);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("maKhoaFilter",    maKhoa);
        model.addAttribute("allTrangThai",    TrangThaiCaThi.values());

        return "admin/ca-thi/danh-sach";
    }

    // ── Chi tiết ──────────────────────────────────────────────────────────────

    @GetMapping("/{ma}")
    public String chiTietCaThi(@PathVariable Integer ma, Model model,
                               RedirectAttributes redirectAttributes) {
        return caThiService.getCaThiByMa(ma).map(caThi -> {
            int soDangKy = caThiService.getDemSoLuongDangKy(List.of(caThi))
                                       .getOrDefault(ma, 0);
            int soBaiThi = caThiService.demSoBaiThiDaNop(ma);
            model.addAttribute("caThi",    caThi);
            model.addAttribute("soDangKy", soDangKy);
            model.addAttribute("soBaiThi", soBaiThi);
            return "admin/ca-thi/chi-tiet";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không tìm thấy ca thi #" + ma);
            return "redirect:/admin/ca-thi";
        });
    }

    // ── Tạo mới ───────────────────────────────────────────────────────────────

    @GetMapping("/tao-moi")
    public String showTaoMoi(Model model) {
        populateFormModel(model, new CaThiFormDTO());
        return "admin/ca-thi/tao-moi";
    }

    @PostMapping("/tao-moi")
    public String processTaoMoi(
            @Valid @ModelAttribute("form") CaThiFormDTO form,
            @RequestParam(name = "fileSinhVien", required = false) MultipartFile fileSinhVien,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateThoiGian(form, bindingResult);

        if (bindingResult.hasErrors()) {
            populateFormModel(model, form);
            return "admin/ca-thi/tao-moi";
        }

        try {
            CaThi saved = caThiService.taoCaThi(form, fileSinhVien);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo ca thi \"" + saved.getTenCaThi() + "\" thành công!");
            return "redirect:/admin/ca-thi";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormModel(model, form);
            return "admin/ca-thi/tao-moi";
        }
    }

    // ── Chỉnh sửa ─────────────────────────────────────────────────────────────

    @GetMapping("/{ma}/chinh-sua")
    public String showChinhSua(@PathVariable Integer ma, Model model,
                               RedirectAttributes redirectAttributes) {
        return caThiService.getCaThiByMa(ma).map(caThi -> {
            model.addAttribute("caThi", caThi);
            populateFormModel(model, caThiService.toFormDTO(caThi));
            return "admin/ca-thi/chinh-sua";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không tìm thấy ca thi #" + ma);
            return "redirect:/admin/ca-thi";
        });
    }

    @PostMapping("/{ma}/chinh-sua")
    public String processChinhSua(
            @PathVariable Integer ma,
            @Valid @ModelAttribute("form") CaThiFormDTO form,
            @RequestParam(name = "fileSinhVien", required = false) MultipartFile fileSinhVien,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateThoiGian(form, bindingResult);

        if (bindingResult.hasErrors()) {
            caThiService.getCaThiByMa(ma).ifPresent(ct -> model.addAttribute("caThi", ct));
            populateFormModel(model, form);
            return "admin/ca-thi/chinh-sua";
        }

        try {
            CaThi updated = caThiService.capNhatCaThi(ma, form, fileSinhVien);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật ca thi \"" + updated.getTenCaThi() + "\" thành công!");
            return "redirect:/admin/ca-thi/" + ma;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            caThiService.getCaThiByMa(ma).ifPresent(ct -> model.addAttribute("caThi", ct));
            populateFormModel(model, form);
            return "admin/ca-thi/chinh-sua";
        }
    }

    // ── Kích hoạt / Kết thúc ─────────────────────────────────────────────────

    /** Giám thị kích hoạt ca thi → DANG_DIEN_RA. */
    @PostMapping("/{ma}/kich-hoat")
    public String kichHoatCaThi(@PathVariable Integer ma, RedirectAttributes redirectAttributes) {
        try {
            CaThi ct = caThiService.kichHoatCaThi(ma);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ca thi \"" + ct.getTenCaThi() + "\" đã được kích hoạt thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ca-thi/" + ma;
    }

    /** Giám thị kết thúc ca thi → DA_KET_THUC. */
    @PostMapping("/{ma}/ket-thuc")
    public String ketThucCaThi(@PathVariable Integer ma, RedirectAttributes redirectAttributes) {
        try {
            CaThi ct = caThiService.ketThucCaThi(ma);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ca thi \"" + ct.getTenCaThi() + "\" đã kết thúc.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ca-thi/" + ma;
    }

    // ── Xóa ───────────────────────────────────────────────────────────────────

    @PostMapping("/{ma}/xoa")
    public String xoaCaThi(@PathVariable Integer ma, RedirectAttributes redirectAttributes) {
        try {
            caThiService.xoaCaThi(ma);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa ca thi thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa ca thi: " + e.getMessage());
        }
        return "redirect:/admin/ca-thi";
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /** Đưa dữ liệu dropdown và form vào model cho trang tạo mới / chỉnh sửa. */
    private void populateFormModel(Model model, CaThiFormDTO form) {
        List<Khoa>  danhSachKhoa  = caThiService.getDanhSachKhoa();
        List<DeThi> danhSachDeThi = caThiService.getDanhSachDeThi();
        model.addAttribute("form",          form);
        model.addAttribute("danhSachKhoa",  danhSachKhoa);
        model.addAttribute("danhSachDeThi", danhSachDeThi);
        model.addAttribute("allTrangThai",  TrangThaiCaThi.values());
    }

    /** Kiểm tra thời gian kết thúc phải sau thời gian bắt đầu. */
    private void validateThoiGian(CaThiFormDTO form, BindingResult bindingResult) {
        if (form.getThoiGianBatDau() != null && form.getThoiGianKetThuc() != null
                && !form.getThoiGianKetThuc().isAfter(form.getThoiGianBatDau())) {
            bindingResult.rejectValue("thoiGianKetThuc", "time.invalid",
                    "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }
}
