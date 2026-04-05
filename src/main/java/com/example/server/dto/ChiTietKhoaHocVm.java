package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKhoaHocVm {
    private Integer maMonHoc;
    private String tenMonHoc;
    private String maDinhDanh;
    private Integer soTinChi;
    private Integer soLuongSinhVien;
    private String lichHoc;
    private String trangThai;
    private List<LichGiangDayVm> lichGiangDay;
    /** Sinh viên gắn kèm phân công (tuỳ chọn), đồng bộ từ giang_vien_mon_hoc. */
    private String sinhVienGanKemHoTen;
    private String sinhVienGanKemMssv;
}
