package com.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ánh xạ bảng cau_hoi_de_thi (câu hỏi thuộc đề thi).
 */
@Entity
@Table(name = "cau_hoi_de_thi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauHoiDeThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_de_thi", nullable = false)
    private DeThi deThi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi", nullable = false)
    private NganHangCauHoi cauHoi;

    @Column(name = "thu_tu_cau_hoi")
    private Integer thuTuCauHoi;

    @Column(name = "diem", precision = 10, scale = 2)
    private BigDecimal diem;

    /** Các câu trả lời của sinh viên cho câu hỏi này trong bài thi. */
    @OneToMany(mappedBy = "cauHoiDeThi", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CauTraLoiSinhVien> cauTraLoiSinhViens = new ArrayList<>();
}
