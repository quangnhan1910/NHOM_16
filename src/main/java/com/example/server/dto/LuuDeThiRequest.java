package com.example.server.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO nhận dữ liệu lưu đề thi từ frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuuDeThiRequest {

    /** Mã đề thi (nếu đang sửa đề thi đã có) */
    private Integer maDeThiHienTai;

    /** Tổng điểm đề thi */
    private BigDecimal tongDiem;

    /** Danh sách câu hỏi */
    private List<CauHoiDTO> cauHois;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CauHoiDTO {
        /** Nội dung câu hỏi */
        private String noiDung;

        /** Loại: true = nhiều đáp án, false = 1 đáp án */
        private boolean isMultipleChoice;

        /** Điểm cho câu hỏi này */
        private BigDecimal diem;

        /** Danh sách lựa chọn */
        private List<LuaChonDTO> luaChons;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LuaChonDTO {
        /** Nhãn: A, B, C, D */
        private String nhan;

        /** Nội dung lựa chọn */
        private String noiDung;

        /** Có phải đáp án đúng? */
        private boolean laDapAnDung;
    }
}
