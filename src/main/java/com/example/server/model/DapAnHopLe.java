package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng dap_an_hop_le (đáp án hợp lệ cho câu hỏi điền đáp án).
 */
@Entity
@Table(name = "dap_an_hop_le")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DapAnHopLe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi", nullable = false)
    private NganHangCauHoi cauHoi;

    @Column(name = "gia_tri_dap_an", nullable = false)
    private String giaTriDapAn;

    @Column(name = "phan_biet_chu_hoa_thuong")
    private Boolean phanBietChuHoaThuong;
}
