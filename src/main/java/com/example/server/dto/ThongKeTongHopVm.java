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
public class ThongKeTongHopVm {
    private List<ThongKeCaThiVm> danhSachCaThi;
    private Integer tongCaThi;
    private Integer tongSinhVien;
    private Integer tongDaNop;
    private Integer tongVang;
    private ThongKeDiemVm diemTongThe;
}
