package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DanhSachSinhVienVm {
    private Integer maMonHoc;
    private String tenMonHoc;
    private Integer tongSoSinhVien;
    private List<SinhVienTrongLopVm> danhSach;
}
