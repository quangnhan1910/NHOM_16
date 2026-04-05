package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Bảng liên kết giảng viên - môn học (giang_vien_mon_hoc).
 */
@Entity
@Table(name = "giang_vien_mon_hoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiangVienMonHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giang_vien", nullable = false)
    private GiangVien giangVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon_hoc", nullable = false)
    private MonHoc monHoc;

    /** Chuyên ngành / nhóm lớp (tùy chọn). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuyen_nganh")
    private ChuyenNganh chuyenNganh;

    /** Sinh viên gắn phân công (tuỳ chọn, có thể nhiều người). */
    @OneToMany(mappedBy = "giangVienMonHoc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ma ASC")
    @Builder.Default
    private List<GiangVienMonHocSinhVien> sinhVienPhanCongs = new ArrayList<>();

    /** Lịch nhập tay: mã / tên ca (tuỳ chọn). */
    @Column(name = "lich_buoi", length = 64)
    private String lichBuoi;

    @Column(name = "lich_ngay")
    private LocalDate lichNgay;

    /** Tiết bắt đầu / kết thúc (số nhập tay, tuỳ chọn). */
    @Column(name = "lich_tiet_bat_dau")
    private Integer lichTietBatDau;

    @Column(name = "lich_tiet_ket_thuc")
    private Integer lichTietKetThuc;

    @Column(name = "lich_phong", length = 128)
    private String lichPhong;

    @Column(name = "lich_noi_dung", length = 512)
    private String lichNoiDung;

    @Column(name = "lich_trang_thai", length = 32)
    private String lichTrangThai;
}
