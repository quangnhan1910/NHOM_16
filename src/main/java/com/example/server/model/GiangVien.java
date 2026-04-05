package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng giang_vien.
 */
@Entity
@Table(name = "giang_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiangVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa", nullable = false)
    private Khoa khoa;

    @Column(name = "ma_nhan_vien")
    private String maNhanVien;

    /** Môn học giảng viên phụ trách. */
    @OneToMany(mappedBy = "giangVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiangVienMonHoc> giangVienMonHocs = new ArrayList<>();

    /** Câu hỏi trong ngân hàng câu hỏi do giảng viên tạo. */
    @OneToMany(mappedBy = "giangVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NganHangCauHoi> nganHangCauHois = new ArrayList<>();

    /** Đề thi do giảng viên tạo. */
    @OneToMany(mappedBy = "giangVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeThi> deThis = new ArrayList<>();
}
