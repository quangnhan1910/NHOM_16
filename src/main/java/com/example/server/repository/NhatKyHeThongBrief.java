package com.example.server.repository;

import java.time.Instant;

/**
 * Projection Spring Data — chỉ các trường cần cho bảng điều khiển (tránh phụ thuộc getter entity).
 */
public interface NhatKyHeThongBrief {
    String getHanhDong();

    String getBangMucTieu();

    Instant getTaoLuc();
}
