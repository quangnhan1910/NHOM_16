package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng mon_hoc.
 */
@Entity
@Table(name = "mon_hoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa", nullable = false)
    private Khoa khoa;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "ma_dinh_danh")
    private String maDinhDanh;

    @Column(name = "so_tin_chi")
    private Integer soTinChi;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Giảng viên phụ trách môn học. */
    @OneToMany(mappedBy = "monHoc", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiangVienMonHoc> giangVienMonHocs = new ArrayList<>();

    /** Ngân hàng câu hỏi của môn học. */
    @OneToMany(mappedBy = "monHoc", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NganHangCauHoi> nganHangCauHois = new ArrayList<>();

    /** Đề thi thuộc môn học. */
    @OneToMany(mappedBy = "monHoc", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeThi> deThis = new ArrayList<>();
}
