package com.example.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * DTO nhận dữ liệu import câu hỏi hàng loạt từ frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportCauHoiRequest {

    private Integer maMonHoc;

    private List<CauHoiItem> cauHois;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CauHoiItem {
        private String questionText;

        @JsonProperty("multipleChoice")
        private Boolean multipleChoice;

        private String mucDoKho; // DE, TRUNG_BINH, KHO, RAT_KHO
        private Double diem;
        private List<OptionItem> options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
        private String text;

        @JsonProperty("correct")
        private Boolean correct;
    }
}
