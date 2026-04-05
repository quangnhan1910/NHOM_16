package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Liên kết nhiều sinh viên tùy chọn với một bản ghi phân công (giang_vien_mon_hoc).
 */
@Entity
@Table(
        name = "giang_vien_mon_hoc_sinh_vien",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_gvmh_ma_sinh_vien",
                columnNames = {"ma_giang_vien_mon_hoc", "ma_sinh_vien"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiangVienMonHocSinhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_giang_vien_mon_hoc", nullable = false)
    private GiangVienMonHoc giangVienMonHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_sinh_vien", nullable = false)
    private SinhVien sinhVien;
}
