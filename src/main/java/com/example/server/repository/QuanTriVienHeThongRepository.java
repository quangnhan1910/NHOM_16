package com.example.server.repository;

import com.example.server.model.QuanTriVienHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository truy vấn bảng quan_tri_vien_he_thong.
 */
@Repository
public interface QuanTriVienHeThongRepository extends JpaRepository<QuanTriVienHeThong, Integer> {

    Optional<QuanTriVienHeThong> findByNguoiDungMa(Integer maNguoiDung);
}
