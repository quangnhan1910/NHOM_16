package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiamSatCaThiVm {
    private Integer maCaThi;
    private String tenCaThi;
    private String tenMonHoc;
    private String maDinhDanhMonHoc;
    private Integer soTinChi;

    private Integer maCaThiNumber;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private String thoiGianHienThi;

    private String phongMay;
    private String diaDiem;

    private String trangThai;

    private Integer tongSoSinhVien;
    private Integer soDaVao;
    private Integer soDangLam;
    private Integer soDaNop;

    private List<GiamSatSinhVienVm> danhSachSinhVien;
}
