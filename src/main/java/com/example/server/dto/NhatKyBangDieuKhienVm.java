package com.example.server.dto;

/**
 * Một dòng nhật ký trên bảng điều khiển quản trị (đã format sẵn cho view).
 *
 * @param badgeLoai hậu tố class CSS: start | add | error | backup | warning
 */
public record NhatKyBangDieuKhienVm(
        String thoiGian,
        String nguoiDung,
        String badgeLoai,
        String hanhDongText,
        String chiTiet,
        String diaChiIp) {}
