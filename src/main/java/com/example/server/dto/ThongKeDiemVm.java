package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDiemVm {
    private BigDecimal diemTrungBinh;
    private BigDecimal tiLeDat;
    private BigDecimal tiLeRot;
    private Integer tongSinhVien;
    private Integer soDat;
    private Integer soRot;
    private Integer soVang;
}
