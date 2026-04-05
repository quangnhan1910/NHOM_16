package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng nganh.
 */
@Entity
@Table(name = "nganh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa", nullable = false)
    private Khoa khoa;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách chuyên ngành thuộc ngành. */
    @OneToMany(mappedBy = "nganh", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChuyenNganh> chuyenNganhs = new ArrayList<>();
}
