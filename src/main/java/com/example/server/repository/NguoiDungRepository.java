package com.example.server.repository;

import com.example.server.model.NguoiDung;
import com.example.server.model.enums.VaiTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng nguoi_dung.
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    Optional<NguoiDung> findByThuDienTu(String thuDienTu);

    Page<NguoiDung> findByVaiTro(VaiTro vaiTro, Pageable pageable);

    List<NguoiDung> findByVaiTro(VaiTro vaiTro);

    Page<NguoiDung> findByTrangThaiHoatDong(Boolean trangThaiHoatDong, Pageable pageable);

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
           "(:keyword IS NULL OR nd.hoTen LIKE %:keyword% OR nd.thuDienTu LIKE %:keyword%) " +
           "AND (:vaiTro IS NULL OR nd.vaiTro = :vaiTro) " +
           "AND (:trangThai IS NULL OR nd.trangThaiHoatDong = :trangThai)")
    Page<NguoiDung> searchNguoiDung(
            @Param("keyword") String keyword,
            @Param("vaiTro") VaiTro vaiTro,
            @Param("trangThai") Boolean trangThai,
            Pageable pageable);

    long countByVaiTro(VaiTro vaiTro);

    boolean existsByThuDienTu(String thuDienTu);

    long countByTrangThaiHoatDong(Boolean trangThaiHoatDong);

    long countByVaiTroAndTaoLucGreaterThanEqual(VaiTro vaiTro, Instant tu);
}
