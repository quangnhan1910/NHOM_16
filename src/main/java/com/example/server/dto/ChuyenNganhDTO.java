package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenNganhDTO {
    private Integer ma;

    @NotBlank(message = "Tên chuyên ngành không được để trống")
    private String ten;

    private Integer maNganh;
}
