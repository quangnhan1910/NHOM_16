package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng lua_chon_cau_hoi (các lựa chọn của câu hỏi trắc nghiệm).
 */
@Entity
@Table(name = "lua_chon_cau_hoi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuaChonCauHoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi", nullable = false)
    private NganHangCauHoi cauHoi;

    @Column(name = "nhan_lua_chon")
    private String nhanLuaChon;

    @Column(name = "noi_dung_lua_chon", columnDefinition = "TEXT")
    private String noiDungLuaChon;

    @Column(name = "la_dap_an_dung")
    private Boolean laDapAnDung;

    @Column(name = "thu_tu_hien_thi")
    private Integer thuTuHienThi;

    /** Các lựa chọn sinh viên đã chọn trong bài làm. */
    @OneToMany(mappedBy = "luaChon", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LuaChonCauTraLoi> luaChonCauTraLois = new ArrayList<>();
}
