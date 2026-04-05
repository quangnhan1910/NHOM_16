package com.example.server.dto;

import com.example.server.model.enums.BacDaoTao;
import com.example.server.model.enums.VaiTro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body khi tạo người dùng mới.
 * Mật khẩu được nhập plaintext, service sẽ hash bằng BCrypt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoNguoiDungRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String thuDienTu;

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String matKhau;

    @NotNull(message = "Vai trò không được để trống")
    private VaiTro vaiTro;

    /** Mã trường — bắt buộc khi vaiTro = QUAN_TRI_VIEN */
    private Integer maTruong;

    /** Mã khoa — bắt buộc khi vaiTro = GIANG_VIEN */
    private Integer maKhoa;

    /** Mã nhân viên — bắt buộc khi vaiTro = GIANG_VIEN */
    private String maNhanVien;

    /** Mã chuyên ngành — bắt buộc khi vaiTro = SINH_VIEN */
    private Integer maChuyenNganh;

    /** Mã sinh viên — bắt buộc khi vaiTro = SINH_VIEN */
    private String maSinhVien;

    /** Bậc đào tạo — bắt buộc khi vaiTro = SINH_VIEN */
    private BacDaoTao bacDaoTao;
}
