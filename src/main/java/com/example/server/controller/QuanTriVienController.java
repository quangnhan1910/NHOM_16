package com.example.server.controller;

import com.example.server.service.BangDieuKhienAdminService;
import com.example.server.service.CaThiService;
import com.example.server.service.HanhDong;
import com.example.server.service.NhatKyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class QuanTriVienController {

    private final BangDieuKhienAdminService bangDieuKhienAdminService;
    private final CaThiService caThiService;
    private final NhatKyService nhatKyService;

    @GetMapping("/admin")
    public String trangQuanTri(Model model) {
        model.addAllAttributes(bangDieuKhienAdminService.layDuLieuBangDieuKhien());
        return "admin/quan-tri-vien";
    }

    /**
     * Kết thúc đồng thời mọi ca thi đang diễn ra (nút khẩn cấp trên bảng điều khiển).
     */
    @PostMapping("/admin/khan-cap/ket-thuc-ca-dang-dien-ra")
    public String khanCapKetThucCaDangDienRa(
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        int n = caThiService.ketThucTatCaDangDienRa();
        if (n > 0) {
            nhatKyService.ghiNhatKy(HanhDong.KHAN_CAP_KET_THUC_CA_THI, "ca_thi", null, request);
        }
        if (n == 0) {
            redirectAttributes.addFlashAttribute(
                    "warningMessage", "Không có ca thi nào đang diễn ra.");
        } else {
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đã kết thúc khẩn cấp " + n + " ca thi đang diễn ra.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/co-cau-to-chuc")
    public String trangCoCauToChuc() {
        return "co-cau-to-chuc/quan-ly-co-cau-to-chuc";
    }

    /** Trang chi tiết trường (hiển thị đầy đủ thông tin). */
    @GetMapping("/admin/co-cau-to-chuc/truong/{id}")
    public String trangChiTietTruong(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("truongId", id);
        return "co-cau-to-chuc/truong-chi-tiet";
    }

    /** Trang sửa trường. */
    @GetMapping("/admin/co-cau-to-chuc/truong/{id}/sua")
    public String trangSuaTruong(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("truongId", id);
        return "co-cau-to-chuc/truong-sua";
    }

    @GetMapping("/admin/quan-ly-mon-hoc")
    public String trangQuanLyMonHoc() {
        return "mon-hoc/quan-ly-mon-hoc";
    }

    /** Phân công giảng viên — môn học — chuyên ngành (nhóm lớp). */
    @GetMapping("/admin/phan-cong-giang-vien-lop")
    public String trangPhanCongGiangVienLop() {
        return "admin/phan-cong-giang-vien-lop";
    }

    @GetMapping("/admin/quan-ly-nguoi-dung")
    public String trangQuanLyNguoiDung() {
        return "nguoi-dung/quan-ly-nguoi-dung";
    }

    @GetMapping("/admin/nhat-ky-he-thong")
    public String trangNhatKyHeThong() {
        return "nhat-ky/nhat-ky-he-thong";
    }

    @GetMapping("/admin/cai-dat-admin")
    public String trangCaiDatAdmin() {
        return "admin/cai-dat-admin";
    }

    /** Trang mặc định sau đăng nhập cho vai trò GIANG_VIEN. */
    @GetMapping("/giang-vien")
    public String trangGiangVien() {
        return "admin/giang-vien";
    }
}
