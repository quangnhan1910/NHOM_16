package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng chuyen_nganh.
 */
@Entity
@Table(name = "chuyen_nganh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuyenNganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nganh", nullable = false)
    private Nganh nganh;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách sinh viên thuộc chuyên ngành. */
    @OneToMany(mappedBy = "chuyenNganh", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SinhVien> sinhViens = new ArrayList<>();
}
