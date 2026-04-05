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
public class NganhDTO {
    private Integer ma;

    @NotBlank(message = "Tên ngành không được để trống")
    private String ten;

    private Integer maKhoa;
}
