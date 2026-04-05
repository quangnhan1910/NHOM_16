package com.example.server.model;

import com.example.server.model.enums.TrangThaiCaThi;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng ca_thi.
 */
@Entity
@Table(name = "ca_thi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_de_thi", nullable = false)
    private DeThi deThi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa", nullable = false)
    private Khoa khoa;

    @Column(name = "ten_ca_thi", nullable = false)
    private String tenCaThi;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private Instant thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc", nullable = false)
    private Instant thoiGianKetThuc;

    @Column(name = "dia_diem")
    private String diaDiem;

    @Column(name = "so_luong_toi_da")
    private Integer soLuongToiDa;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiCaThi trangThai;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách đăng ký thi của ca này. */
    @OneToMany(mappedBy = "caThi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DangKyThi> dangKyThis = new ArrayList<>();

    /** Danh sách bài thi sinh viên đã làm trong ca này. */
    @OneToMany(mappedBy = "caThi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BaiThiSinhVien> baiThiSinhViens = new ArrayList<>();
}
