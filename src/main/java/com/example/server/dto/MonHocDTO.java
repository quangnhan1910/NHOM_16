package com.example.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonHocDTO {

    private Integer ma;

    @NotBlank(message = "Mã môn học không được để trống.")
    private String maDinhDanh;

    @NotBlank(message = "Tên môn học không được để trống.")
    private String ten;

    @NotNull(message = "Số tín chỉ không được để trống.")
    @Min(value = 1, message = "Số tín chỉ phải từ 1 trở lên.")
    @Max(value = 12, message = "Số tín chỉ không được vượt quá 12.")
    private Integer soTinChi;

    @NotNull(message = "Khoa không được để trống.")
    private Integer maKhoa;

    private String tenKhoa;

    private java.time.Instant taoLuc;
    private java.time.Instant capNhatLuc;
}
