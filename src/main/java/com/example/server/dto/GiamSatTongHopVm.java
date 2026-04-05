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
public class GiamSatTongHopVm {
    private List<GiamSatCaThiVm> danhSachCaThi;
    private Integer tongCaThi;
    private Integer tongSinhVienDangThi;
}
