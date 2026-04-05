package com.example.server.repository;

import com.example.server.model.MonHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository truy vấn bảng mon_hoc.
 */
@Repository
public interface MonHocRepository extends JpaRepository<MonHoc, Integer> {

    Page<MonHoc> findByKhoaMa(Integer maKhoa, Pageable pageable);

    @Query("SELECT m FROM MonHoc m WHERE " +
           "(:keyword IS NULL OR m.ten LIKE %:keyword% OR m.maDinhDanh LIKE %:keyword%) " +
           "AND (:maKhoa IS NULL OR m.khoa.ma = :maKhoa) " +
           "AND (:soTinChi IS NULL OR m.soTinChi = :soTinChi)")
    Page<MonHoc> searchMonHoc(
            @Param("keyword") String keyword,
            @Param("maKhoa") Integer maKhoa,
            @Param("soTinChi") Integer soTinChi,
            Pageable pageable);

    boolean existsByMaDinhDanh(String maDinhDanh);
}
