package com.example.server.model.enums;

/**
 * Loại đề thi (giữa kỳ, cuối kỳ, thực hành, ...).
 */
public enum LoaiDeThi {
    GIUA_KY("Giữa kỳ"),
    CUOI_KY("Cuối kỳ"),
    THUC_HANH("Thực hành");

    private final String hienThi;

    LoaiDeThi(String hienThi) {
        this.hienThi = hienThi;
    }

    public String getHienThi() {
        return hienThi;
    }
}
