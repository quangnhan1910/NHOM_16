package com.example.server.model;

import com.example.server.model.enums.TrangThaiBaiThi;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng bai_thi_sinh_vien (bài làm của sinh viên trong một ca thi).
 */
@Entity
@Table(name = "bai_thi_sinh_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaiThiSinhVien {

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

    @Column(name = "bat_dau_luc")
    private Instant batDauLuc;

    @Column(name = "nop_bai_luc")
    private Instant nopBaiLuc;

    @Column(name = "thoi_gian_lam_bai_giay")
    private Integer thoiGianLamBaiGiay;

    @Column(name = "tong_diem", precision = 10, scale = 2)
    private BigDecimal tongDiem;

    @Column(name = "dat")
    private Boolean dat;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiBaiThi trangThai;

    @Column(name = "tu_dong_nop")
    private Boolean tuDongNop;

    @Column(name = "dia_chi_ip")
    private String diaChiIp;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    /** Danh sách câu trả lời trong bài thi. */
    @OneToMany(mappedBy = "baiThiSinhVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CauTraLoiSinhVien> cauTraLoiSinhViens = new ArrayList<>();
}
