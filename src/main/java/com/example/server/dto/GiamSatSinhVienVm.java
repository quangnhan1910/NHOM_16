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
public class GiamSatSinhVienVm {
    private Integer maBaiThi;
    private Integer maSinhVien;
    private String maSinhVienStr;
    private String hoVaTen;
    private String lopSinhHoat;

    private LocalDateTime batDauLuc;
    private LocalDateTime nopBaiLuc;
    private Integer thoiGianLamBaiPhut;

    private String trangThai;
    private BigDecimal tongDiem;

    private String diaChiIp;
    private Boolean daCoMatKhau;
    private Integer soCauDaTraLoi;
    private Integer tongCauHoi;
}
