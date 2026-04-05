package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinhVienTrongLopVm {
    private Integer maSinhVien;
    private String maSinhVienStr; // MSSV dạng chuỗi (SV21001)
    private String hoVaTen;
    private String lopSinhHoat;
    private String email;
    private String tinhTrang; // DU_DIEU_KIEN, CANH_BAO_VANG, CAM_THI
}
