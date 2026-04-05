package com.example.server.repository;

import com.example.server.model.NhatKyHeThong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository truy vấn bảng nhat_ky_he_thong.
 */
@Repository
public interface NhatKyHeThongRepository extends JpaRepository<NhatKyHeThong, Integer> {

    List<NhatKyHeThong> findTop10ByOrderByTaoLucDesc();

    List<NhatKyHeThong> findTop5ByOrderByTaoLucDesc();

    List<NhatKyHeThongBrief> findTop10BriefByOrderByTaoLucDesc();

    @Query("SELECT nk FROM NhatKyHeThong nk WHERE " +
           "(:keyword IS NULL OR nk.hanhDong LIKE %:keyword% OR nk.bangMucTieu LIKE %:keyword%) " +
           "AND (:vaiTro IS NULL OR nk.vaiTroNguoiThucHien = :vaiTro) " +
           "AND (:hanhDong IS NULL OR nk.hanhDong = :hanhDong) " +
           "AND (:tuNgay IS NULL OR nk.taoLuc >= :tuNgay) " +
           "AND (:denNgay IS NULL OR nk.taoLuc <= :denNgay)")
    Page<NhatKyHeThong> searchNhatKyHeThong(
            @Param("keyword") String keyword,
            @Param("vaiTro") String vaiTro,
            @Param("hanhDong") String hanhDong,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable);
}
