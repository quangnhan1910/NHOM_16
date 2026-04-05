package com.example.server.model;

import com.example.server.model.enums.BacDaoTao;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng sinh_vien.
 */
@Entity
@Table(name = "sinh_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuyen_nganh", nullable = false)
    private ChuyenNganh chuyenNganh;

    @Column(name = "ma_sinh_vien")
    private String maSinhVien;

    @Enumerated(EnumType.STRING)
    @Column(name = "bac_dao_tao")
    private BacDaoTao bacDaoTao;

    /** Danh sách đăng ký thi. */
    @OneToMany(mappedBy = "sinhVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DangKyThi> dangKyThis = new ArrayList<>();

    /** Danh sách bài thi đã làm. */
    @OneToMany(mappedBy = "sinhVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BaiThiSinhVien> baiThiSinhViens = new ArrayList<>();
}
