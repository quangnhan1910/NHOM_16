package com.example.server.repository;

import com.example.server.model.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng sinh_vien.
 */
@Repository
public interface SinhVienRepository extends JpaRepository<SinhVien, Integer> {

    Optional<SinhVien> findByMaSinhVien(String maSinhVien);

    boolean existsByMaSinhVien(String maSinhVien);

    Optional<SinhVien> findByNguoiDungMa(Integer maNguoiDung);

    @Query("SELECT DISTINCT sv FROM SinhVien sv JOIN FETCH sv.nguoiDung ORDER BY sv.ma")
    List<SinhVien> findAllCoNguoiDung();

    /** Tìm sinh viên kèm JOIN FETCH NguoiDung để xác thực đăng nhập. */
    @Query("""
            SELECT sv FROM SinhVien sv
            LEFT JOIN FETCH sv.nguoiDung
            WHERE sv.maSinhVien = :maSinhVien
            """)
    Optional<SinhVien> findByMaSinhVienWithNguoiDung(@Param("maSinhVien") String maSinhVien);
}
