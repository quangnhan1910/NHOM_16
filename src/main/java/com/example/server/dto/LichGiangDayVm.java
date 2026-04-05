package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichGiangDayVm {
    /** Ca / mã (nhập tay hoặc từ ca thi). */
    private String buoi;
    private LocalDate ngay;
    private LocalTime thoiGianBatDau;
    private LocalTime thoiGianKetThuc;
    /** Tiết bắt đầu/kết thúc (lịch nhập tay); ca thi thì thường null. */
    private Integer tietBatDau;
    private Integer tietKetThuc;
    private String phong;
    private String noiDung;
    private String trangThai; // DA_DAY, SAI_TOI, CHUA_DAY
}
