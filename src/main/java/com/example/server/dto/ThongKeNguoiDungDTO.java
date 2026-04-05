package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thống kê tổng quan người dùng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeNguoiDungDTO {

    private long tongNguoiDung;
    private long tongQuanTriVien;
    private long tongGiangVien;
    private long tongSinhVien;
    private long tongDangHoatDong;
    private long tongBiKhoa;
}
