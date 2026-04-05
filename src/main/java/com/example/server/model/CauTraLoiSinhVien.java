package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng cau_tra_loi_sinh_vien (câu trả lời của sinh viên cho một câu hỏi trong bài thi).
 */
@Entity
@Table(name = "cau_tra_loi_sinh_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauTraLoiSinhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_thi_sinh_vien", nullable = false)
    private BaiThiSinhVien baiThiSinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi_de_thi", nullable = false)
    private CauHoiDeThi cauHoiDeThi;

    @Column(name = "cau_tra_loi_van_ban")
    private String cauTraLoiVanBan;

    @Column(name = "la_dap_an_dung")
    private Boolean laDapAnDung;

    @Column(name = "diem_dat_duoc", precision = 10, scale = 2)
    private BigDecimal diemDatDuoc;

    @Column(name = "tra_loi_luc")
    private Instant traLoiLuc;

    /** Các lựa chọn sinh viên đã chọn (với câu trắc nghiệm). */
    @OneToMany(mappedBy = "cauTraLoiSinhVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LuaChonCauTraLoi> luaChonCauTraLois = new ArrayList<>();
}
