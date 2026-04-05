package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng lua_chon_cau_tra_loi (lựa chọn mà sinh viên chọn cho câu trắc nghiệm).
 */
@Entity
@Table(name = "lua_chon_cau_tra_loi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuaChonCauTraLoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_tra_loi_sinh_vien", nullable = false)
    private CauTraLoiSinhVien cauTraLoiSinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lua_chon", nullable = false)
    private LuaChonCauHoi luaChon;
}
