package com.example.server.dto;

import com.example.server.model.enums.BacDaoTao;
import com.example.server.model.enums.VaiTro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO cho người dùng.
 * KHÔNG bao gồm matKhauMaHoa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDungDTO {

    private Integer ma;
    private String thuDienTu;
    private String hoTen;
    private VaiTro vaiTro;
    private Boolean trangThaiHoatDong;
    private Instant taoLuc;
    private Instant capNhatLuc;

    // Profile fields — phụ thuộc vai trò
    private Integer maTruong;         // cho QUAN_TRI_VIEN
    private Integer maKhoa;            // cho GIANG_VIEN
    private String maNhanVien;         // cho GIANG_VIEN
    private Integer maChuyenNganh;     // cho SINH_VIEN
    private String maSinhVien;        // cho SINH_VIEN
    private BacDaoTao bacDaoTao;       // cho SINH_VIEN
}
