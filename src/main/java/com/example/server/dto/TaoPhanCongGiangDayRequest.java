package com.example.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Thân yêu cầu tạo phân công giảng viên — môn học — (tùy chọn) chuyên ngành/lớp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoPhanCongGiangDayRequest {

    @NotNull
    private Integer maGiangVien;

    @NotNull
    private Integer maMonHoc;

    /** Null = không gắn chuyên ngành cụ thể (áp dụng cả khóa / mặc định). */
    private Integer maChuyenNganh;

    /** Nhiều sinh viên tuỳ chọn (có thể rỗng). */
    private List<Integer> maSinhViens;

    /** Một sinh viên (API cũ); nếu có thì dùng khi maSinhViens null/rỗng. */
    private Integer maSinhVien;

    /** Lịch nhập tay (tuỳ chọn). Ngày yyyy-MM-dd; tiết là số nguyên (chuỗi). */
    private String lichBuoi;
    private String lichNgay;
    private String lichTietBatDau;
    private String lichTietKetThuc;
    private String lichPhong;
    private String lichNoiDung;
    /** CHUA_DAY | SAI_TOI | DA_DAY hoặc để trống. */
    private String lichTrangThai;
}
