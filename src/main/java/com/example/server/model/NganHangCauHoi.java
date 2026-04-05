package com.example.server.model;

import com.example.server.model.enums.LoaiCauHoi;
import com.example.server.model.enums.MucDoKho;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng ngan_hang_cau_hoi (ngân hàng câu hỏi).
 */
@Entity
@Table(name = "ngan_hang_cau_hoi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NganHangCauHoi {

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

    @Column(name = "noi_dung", columnDefinition = "TEXT", nullable = false)
    private String noiDung;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_cau_hoi", nullable = false)
    private LoaiCauHoi loaiCauHoi;

    @Enumerated(EnumType.STRING)
    @Column(name = "muc_do_kho")
    private MucDoKho mucDoKho;

    @Column(name = "diem", precision = 10, scale = 2)
    private BigDecimal diem;

    @Column(name = "trang_thai_hoat_dong")
    private Boolean trangThaiHoatDong;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Các lựa chọn của câu hỏi trắc nghiệm. */
    @OneToMany(mappedBy = "cauHoi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LuaChonCauHoi> luaChonCauHois = new ArrayList<>();

    /** Đáp án hợp lệ (cho câu điền đáp án). */
    @OneToMany(mappedBy = "cauHoi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DapAnHopLe> dapAnHopLes = new ArrayList<>();

    /** Các đề thi có chứa câu hỏi này. */
    @OneToMany(mappedBy = "cauHoi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CauHoiDeThi> cauHoiDeThis = new ArrayList<>();
}
