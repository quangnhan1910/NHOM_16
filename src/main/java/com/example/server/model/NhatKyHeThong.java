package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity ánh xạ bảng nhat_ky_he_thong (nhật ký / audit log hệ thống).
 */
@Entity
@Table(name = "nhat_ky_he_thong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKyHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @Column(name = "vai_tro_nguoi_thuc_hien")
    private String vaiTroNguoiThucHien;

    @Column(name = "ma_nguoi_thuc_hien")
    private Integer maNguoiThucHien;

    @Column(name = "hanh_dong", nullable = false)
    private String hanhDong;

    @Column(name = "bang_muc_tieu")
    private String bangMucTieu;

    @Column(name = "ma_muc_tieu")
    private Integer maMucTieu;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "dia_chi_ip")
    private String diaChiIp;
}
