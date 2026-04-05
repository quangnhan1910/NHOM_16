package com.example.server.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO trả về JSON cho câu hỏi trong ngân hàng câu hỏi.
 * Flatten data để tránh circular reference khi serialize.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NganHangCauHoiDTO {

    private Integer ma;
    private String noiDung;

    // Thông tin môn học (flatten)
    private Integer maMonHoc;
    private String tenMonHoc;

    // Enum values dạng String
    private String loaiCauHoi;
    private String mucDoKho;

    private BigDecimal diem;
    private Boolean trangThaiHoatDong;
}
