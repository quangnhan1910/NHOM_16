package com.example.server.repository;

import com.example.server.model.NganHangCauHoi;
import com.example.server.model.enums.LoaiCauHoi;
import com.example.server.model.enums.MucDoKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

/**
 * Repository truy vấn bảng ngan_hang_cau_hoi.
 */
public interface NganHangCauHoiRepository extends JpaRepository<NganHangCauHoi, Integer> {

    /**
     * Tìm kiếm câu hỏi với các bộ lọc tùy chọn (tất cả đều nullable).
     * Nếu tham số = null thì bỏ qua điều kiện đó.
     */
    @Query(value = "SELECT c FROM NganHangCauHoi c " +
            "JOIN FETCH c.monHoc m " +
            "WHERE (:maMonHoc IS NULL OR m.ma = :maMonHoc) " +
            "AND (:loaiCauHoi IS NULL OR c.loaiCauHoi = :loaiCauHoi) " +
            "AND (:mucDoKho IS NULL OR c.mucDoKho = :mucDoKho) " +
            "AND (:tuKhoa IS NULL OR LOWER(c.noiDung) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))",
            countQuery = "SELECT COUNT(c) FROM NganHangCauHoi c " +
            "JOIN c.monHoc m " +
            "WHERE (:maMonHoc IS NULL OR m.ma = :maMonHoc) " +
            "AND (:loaiCauHoi IS NULL OR c.loaiCauHoi = :loaiCauHoi) " +
            "AND (:mucDoKho IS NULL OR c.mucDoKho = :mucDoKho) " +
            "AND (:tuKhoa IS NULL OR LOWER(c.noiDung) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))")
    Page<NganHangCauHoi> timKiem(
            @Param("maMonHoc") Integer maMonHoc,
            @Param("loaiCauHoi") LoaiCauHoi loaiCauHoi,
            @Param("mucDoKho") MucDoKho mucDoKho,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM NganHangCauHoi c WHERE c.taoLuc IS NOT NULL AND c.taoLuc >= :tu")
    long demTaoTu(@Param("tu") Instant tu);
}
