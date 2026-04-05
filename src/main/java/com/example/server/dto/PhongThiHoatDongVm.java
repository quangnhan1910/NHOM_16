package com.example.server.dto;

/**
 * Một dòng "phòng thi" suy ra từ ca thi đang diễn ra (địa điểm + đăng ký / sức chứa).
 *
 * @param dotLoai green | yellow | neutral — map tới room-status-dot trong admin.css
 */
public record PhongThiHoatDongVm(String tenHienThi, String dongPhai, String dotLoai) {}
