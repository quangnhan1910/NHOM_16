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
public class GiamSatTheoDoiCauVm {
    private Integer maCauHoiDeThi;
    private String noiDungCauHoi;
    private Boolean laDapAnDung;
    private List<GiamSatTheoDoiLuaChonVm> luaChonDaChon;
}
