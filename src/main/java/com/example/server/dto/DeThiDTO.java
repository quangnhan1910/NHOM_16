package com.example.server.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO hiển thị thông tin đề thi trên bảng quản lý.
 */
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeThiDTO {

    private Integer ma;
    private String tenDeThi;
    private String tenMonHoc;
    private Integer maMonHoc;
    private String loaiDeThi;
    private String tenLoaiDeThi;
    private Integer thoiLuongPhut;
    private BigDecimal tongDiem;
    private BigDecimal diemDat;
    private Boolean xaoTronCauHoi;
    private Boolean xaoTronLuaChon;
    private Boolean daXuatBan;
    private Integer soCauHoi;
    private Integer maGiangVien;
    private String hoTenGiangVien;
    private Instant taoLuc;
    private Instant capNhatLuc;
}
