package com.example.server.model;

import com.example.server.model.enums.VaiTro;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng nguoi_dung.
 */
@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @Column(name = "thu_dien_tu", nullable = false)
    private String thuDienTu;

    @Column(name = "mat_khau_ma_hoa", nullable = false)
    private String matKhauMaHoa;

    @Column(name = "ho_ten")
    private String hoTen;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "trang_thai_hoat_dong")
    private Boolean trangThaiHoatDong;

    @Column(name = "tao_luc")
    private Instant taoLuc;

    @Column(name = "cap_nhat_luc")
    private Instant capNhatLuc;

    /** Danh sách vai trò quản trị viên hệ thống (nếu có). */
    @OneToMany(mappedBy = "nguoiDung", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuanTriVienHeThong> quanTriVienHeThongs = new ArrayList<>();

    /** Danh sách vai trò giảng viên (nếu có). */
    @OneToMany(mappedBy = "nguoiDung", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiangVien> giangViens = new ArrayList<>();

    /** Danh sách vai trò sinh viên (nếu có). */
    @OneToMany(mappedBy = "nguoiDung", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SinhVien> sinhViens = new ArrayList<>();
}
