package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiamSatTheoDoiLuaChonVm {
    private Integer maLuaChon;
    private String noiDungLuaChon;
}
