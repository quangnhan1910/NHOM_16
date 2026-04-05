package com.example.server.service;

/**
 * Các hành động được ghi vào nhật ký hệ thống.
 * Mỗi giá trị gồm: mã (dùng lưu DB) và mô tả tiếng Việt.
 */
public enum HanhDong {

    // Cơ cấu tổ chức
    THEM_TRUONG("Thêm trường", "truong"),
    SUA_TRUONG("Sửa trường", "truong"),
    XOA_TRUONG("Xóa trường", "truong"),
    THEM_KHOA("Thêm khoa", "khoa"),
    SUA_KHOA("Sửa khoa", "khoa"),
    XOA_KHOA("Xóa khoa", "khoa"),
    THEM_NGANH("Thêm ngành", "nganh"),
    SUA_NGANH("Sửa ngành", "nganh"),
    XOA_NGANH("Xóa ngành", "nganh"),
    THEM_CHUYEN_NGANH("Thêm chuyên ngành", "chuyen_nganh"),
    SUA_CHUYEN_NGANH("Sửa chuyên ngành", "chuyen_nganh"),
    XOA_CHUYEN_NGANH("Xóa chuyên ngành", "chuyen_nganh"),

    // Người dùng
    THEM_NGUOI_DUNG("Thêm người dùng", "nguoi_dung"),
    SUA_NGUOI_DUNG("Sửa người dùng", "nguoi_dung"),
    XOA_NGUOI_DUNG("Xóa người dùng", "nguoi_dung"),
    KHOA_TAI_KHOAN("Khóa tài khoản", "nguoi_dung"),
    MO_TAI_KHOAN("Mở khóa tài khoản", "nguoi_dung"),
    DOI_MAT_KHAU("Đổi mật khẩu", "nguoi_dung"),
    DANG_NHAP("Đăng nhập", "nguoi_dung"),
    DANG_XUAT("Đăng xuất", "nguoi_dung"),

    // Môn học
    THEM_MON_HOC("Thêm môn học", "mon_hoc"),
    SUA_MON_HOC("Sửa môn học", "mon_hoc"),
    XOA_MON_HOC("Xóa môn học", "mon_hoc"),

    // Đề thi / Ca thi / Câu hỏi (tham khảo, chưa cần)
    THEM_DE_THI("Tạo đề thi", "de_thi"),
    XUAT_BAN_DE_THI("Xuất bản đề thi", "de_thi"),
    TAO_CA_THI("Tạo ca thi", "ca_thi"),
    HUY_CA_THI("Hủy ca thi", "ca_thi"),
    /** Quản trị viên kết thúc đồng thời mọi ca đang diễn ra (khẩn cấp). */
    KHAN_CAP_KET_THUC_CA_THI("Kết thúc khẩn cấp toàn bộ ca thi đang diễn ra", "ca_thi"),
    THEM_CAU_HOI("Thêm câu hỏi", "ngan_hang_cau_hoi"),
    SUA_CAU_HOI("Sửa câu hỏi", "ngan_hang_cau_hoi"),

    // Hệ thống
    DOI_CAI_DAT("Thay đổi cài đặt", "cau_hinh");

    private final String moTa;
    private final String bangMacDinh;

    HanhDong(String moTa, String bangMacDinh) {
        this.moTa = moTa;
        this.bangMacDinh = bangMacDinh;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getBangMacDinh() {
        return bangMacDinh;
    }
}
