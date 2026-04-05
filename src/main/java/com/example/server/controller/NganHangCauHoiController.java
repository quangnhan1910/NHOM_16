package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller serve các trang HTML cho ngân hàng câu hỏi.
 */
@Controller
public class NganHangCauHoiController {

    /**
     * Trang danh sách ngân hàng câu hỏi.
     */
    @GetMapping("/ngan-hang-cau-hoi")
    public String nganHangCauHoi() {
        return "nganhangcauhoi/ngan-hang-cau-hoi";
    }

    /**
     * Trang thêm câu hỏi mới (Import file)
     */
    @GetMapping("/ngan-hang-cau-hoi/them")
    public String themCauHoi() {
        return "nganhangcauhoi/them-cau-hoi";
    }

    /**
     * Trang chỉnh sửa nội dung sau khi import file (Raw Editor & Preview)
     */
    @GetMapping("/ngan-hang-cau-hoi/chinh-sua")
    public String chinhSuaCauHoi() {
        return "nganhangcauhoi/chinh-sua-cau-hoi";
    }

    /**
     * Trang kiểm tra & xác nhận dữ liệu trước khi lưu
     */
    @GetMapping("/ngan-hang-cau-hoi/kiem-tra")
    public String kiemTraCauHoi() {
        return "nganhangcauhoi/kiem-tra-cau-hoi";
    }

    /**
     * Trang thông báo import thành công
     */
    @GetMapping("/ngan-hang-cau-hoi/thanh-cong")
    public String importThanhCong() {
        return "nganhangcauhoi/import-thanh-cong";
    }
}
