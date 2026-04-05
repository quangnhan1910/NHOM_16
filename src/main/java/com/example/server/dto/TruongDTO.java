package com.example.server.dto;

import com.example.server.model.enums.CapBacTruong;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruongDTO {
    private Integer ma;

    @NotBlank(message = "Tên trường không được để trống")
    private String ten;

    @NotBlank(message = "Cấp bậc không được để trống")
    private CapBacTruong capBac;

    @NotBlank(message = "Mã định danh không được để trống")
    private String maDinhDanh;

    private String diaChi;
}
