package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhoaHocCuaToiVm {
    private Integer maMonHoc;
    private String tenMonHoc;
    private String maDinhDanh;
    private Integer soTinChi;
    private Integer soLuongSinhVien;
    private String lichHoc;
    private String trangThai; // DANG_DIEN_RA, SAP_BAT_DAU, LEN_KE_HOACH
    private Instant thoiGianBatDau;
    private Instant thoiGianKetThuc;
    private Integer maGiangVienMonHoc;
}
