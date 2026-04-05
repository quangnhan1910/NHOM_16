package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiamSatTheoDoiBaiVm {
    private Integer maBaiThi;
    private Integer maCaThi;
    private String tenCaThi;
    private String tenMonHoc;
    private String maDinhDanhMonHoc;

    private String maSinhVienStr;
    private String hoVaTen;
    private String lopSinhHoat;

    private LocalDateTime batDauLuc;
    private LocalDateTime nopBaiLuc;
    private String trangThai;
    private BigDecimal tongDiem;
    private Integer soCauDaTraLoi;
    private Integer tongCauHoi;

    /** true khi SV đang làm: server chưa có câu trả lời cho đến khi nộp bài */
    private Boolean canhBaoChuaLuuTraLoiLenServer;

    private List<GiamSatTheoDoiCauVm> chiTietCauTraLoi;
}
