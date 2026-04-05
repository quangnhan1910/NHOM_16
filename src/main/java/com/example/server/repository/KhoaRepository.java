package com.example.server.repository;

import com.example.server.model.Khoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository truy vấn bảng khoa.
 */
@Repository
public interface KhoaRepository extends JpaRepository<Khoa, Integer> {

    @Query("SELECT k FROM Khoa k WHERE " +
           "(:keyword IS NULL OR k.ten LIKE %:keyword%)")
    Page<Khoa> searchKhoa(@Param("keyword") String keyword, Pageable pageable);

    List<Khoa> findByTruongMa(Integer maTruong);

    boolean existsByTruongMa(Integer maTruong);

    @Query("SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END FROM Khoa k WHERE k.truong.ma = :maTruong")
    boolean coKhoaThuocTruong(@Param("maTruong") Integer maTruong);

    /**
     * Kiểm tra khoa có ngành hay không.
     * Dùng trong KhoaService.xoa.
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Nganh n WHERE n.khoa.ma = :maKhoa")
    boolean coNganhThuocKhoa(@Param("maKhoa") Integer maKhoa);

    @Query("SELECT COUNT(k) FROM Khoa k WHERE k.taoLuc IS NOT NULL AND k.taoLuc >= :tu")
    long demTaoTu(@Param("tu") Instant tu);
}
