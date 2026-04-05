package com.example.server.model;

import com.example.server.model.enums.LoaiDeThi;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng de_thi.
 */
@Entity
@Table(name = "de_thi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon_hoc", nullable = false)
    private MonHoc monHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giang_vien", nullable = false)
    private GiangVien giangVien;

    @Column(name = "ten_de_thi", nullable = false)
    private String tenDeThi;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_de_thi")
    private LoaiDeThi loaiDeThi;

    @Column(name = "thoi_luong_phut")
    private Integer thoiLuongPhut;

    @Column(name = "tong_diem", precision = 10, scale = 2)
    private BigDecimal tongDiem;

    @Column(name = "diem_dat", precision = 10, scale = 2)
    private BigDecimal diemDat;

    @Column(name = "xao_tron_cau_hoi")
    private Boolean xaoTronCauHoi;

    @Column(name = "xao_tron_lua_chon")
    private Boolean xaoTronLuaChon;

    @Column(name = "da_xuat_ban")
    private Boolean daXuatBan;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách câu hỏi trong đề thi. */
    @OneToMany(mappedBy = "deThi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CauHoiDeThi> cauHoiDeThis = new ArrayList<>();

    /** Danh sách ca thi sử dụng đề này. */
    @OneToMany(mappedBy = "deThi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaThi> caThis = new ArrayList<>();
}
