package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng quan_tri_vien_he_thong.
 */
@Entity
@Table(name = "quan_tri_vien_he_thong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuanTriVienHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_truong", nullable = false)
    private Truong truong;
}
