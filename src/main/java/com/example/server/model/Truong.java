package com.example.server.model;

import com.example.server.model.enums.CapBacTruong;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng truong.
 */
@Entity
@Table(name = "truong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Enumerated(EnumType.STRING)
    @Column(name = "cap_bac", length = 20)
    private CapBacTruong capBac;

    @Column(name = "ma_dinh_danh")
    private String maDinhDanh;

    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách khoa thuộc trường. */
    @OneToMany(mappedBy = "truong", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Khoa> khoas = new ArrayList<>();

    /** Danh sách quản trị viên hệ thống của trường. */
    @OneToMany(mappedBy = "truong", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuanTriVienHeThong> quanTriVienHeThongs = new ArrayList<>();
}
