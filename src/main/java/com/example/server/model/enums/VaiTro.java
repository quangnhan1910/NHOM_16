package com.example.server.model.enums;

/**
 * Vai trò người dùng trong hệ thống.
 */
public enum VaiTro {
    QUAN_TRI_VIEN("Quản trị viên"),
    GIANG_VIEN("Giảng viên"),
    SINH_VIEN("Sinh viên");

    private final String hienThi;

    VaiTro(String hienThi) {
        this.hienThi = hienThi;
    }

    public String getHienThi() {
        return hienThi;
    }
}
