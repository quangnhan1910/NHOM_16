package com.example.server.model;

import com.example.server.model.enums.TrangThaiDangKy;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity ánh xạ bảng dang_ky_thi.
 */
@Entity
@Table(name = "dang_ky_thi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangKyThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ca_thi", nullable = false)
    private CaThi caThi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_sinh_vien", nullable = false)
    private SinhVien sinhVien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_dang_ky")
    private TrangThaiDangKy trangThaiDangKy;

    @Column(name = "dang_ky_luc")
    private Instant dangKyLuc;

    /** Giám thị đã xác nhận sinh viên có mặt tại phòng thi. */
    @Column(name = "da_check_in", nullable = false)
    @Builder.Default
    private Boolean daCheckIn = false;

    /** Thời điểm check-in (null nếu chưa check-in). */
    @Column(name = "check_in_luc")
    private Instant checkInLuc;
}
