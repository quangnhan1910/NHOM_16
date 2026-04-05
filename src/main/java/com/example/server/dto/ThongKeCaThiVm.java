package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeCaThiVm {
    private Integer maCaThi;
    private String tenCaThi;
    private String tenMonHoc;
    private String maDinhDanhMonHoc;
    private Integer soTinChi;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private String thoiGianHienThi;
    private String diaDiem;
    private String trangThai;
    private Integer tongSoSinhVien;
    private Integer soDaNop;
    private Integer soVang;
    private Integer soDat;
    private Integer soRot;
    private BigDecimal diemTrungBinh;
    private BigDecimal tiLeDat;
    private Integer cauHoiKhoNhatMa;
    private String cauHoiKhoNhatNoiDung;
    private BigDecimal tiLeTraLoiDungCauHoiKho;
}
