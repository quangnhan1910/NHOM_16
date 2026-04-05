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
public class KetQuaThiSinhVienVm {
    private Integer maBaiThi;
    private Integer maSinhVien;
    private String maSinhVienStr;
    private String hoVaTen;
    private String lopSinhHoat;
    private LocalDateTime thoiGianNop;
    private Integer thoiGianLamBaiPhut;
    private BigDecimal tongDiem;
    private Boolean dat;
    private String xepLoai; // XUAT_SAC, KHA, TRUNG_BINH, YEU, KEM
}
