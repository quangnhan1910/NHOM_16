package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng khoa.
 */
@Entity
@Table(name = "khoa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Khoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_truong", nullable = false)
    private Truong truong;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách ngành thuộc khoa. */
    @OneToMany(mappedBy = "khoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Nganh> nganhs = new ArrayList<>();

    /** Danh sách môn học thuộc khoa. */
    @OneToMany(mappedBy = "khoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MonHoc> monHocs = new ArrayList<>();

    /** Danh sách giảng viên thuộc khoa. */
    @OneToMany(mappedBy = "khoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiangVien> giangViens = new ArrayList<>();

    /** Danh sách ca thi do khoa tổ chức. */
    @OneToMany(mappedBy = "khoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaThi> caThis = new ArrayList<>();
}
