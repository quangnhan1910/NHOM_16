package com.example.server.service;

import com.example.server.model.NguoiDung;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service ghi nhật ký hệ thống (audit log).
 * Dùng chung cho mọi module: Cơ cấu tổ chức, Người dùng, Môn học, Cài đặt...
 */
public interface NhatKyService {

    /**
     * Ghi một bản ghi nhật ký hệ thống.
     * Nếu bangMucTieu là null hoặc blank, sẽ dùng giá trị mặc định từ enum HanhDong.
     *
     * @param hanhDong    hành động thực hiện
     * @param bangMucTieu bảng bị tác động (null → tự dùng default từ enum)
     * @param maMucTieu   khóa chính của bản ghi bị tác động
     * @param request     HttpServletRequest hiện tại (để lấy IP)
     */
    void ghiNhatKy(HanhDong hanhDong, String bangMucTieu, Integer maMucTieu, HttpServletRequest request);

    /**
     * Lấy NguoiDung hiện tại từ SecurityContextHolder.
     * Trả về null nếu chưa đăng nhập hoặc principal không phải CustomUserDetails.
     */
    NguoiDung layNguoiDungHienTai();
}
