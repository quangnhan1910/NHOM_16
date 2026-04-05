package com.example.server.repository;

import com.example.server.model.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng giang_vien.
 */
@Repository
public interface GiangVienRepository extends JpaRepository<GiangVien, Integer> {

    Optional<GiangVien> findByMaNhanVien(String maNhanVien);

    boolean existsByMaNhanVien(String maNhanVien);

    Optional<GiangVien> findByNguoiDungMa(Integer maNguoiDung);

    @Query("SELECT DISTINCT gv FROM GiangVien gv JOIN FETCH gv.nguoiDung ORDER BY gv.ma")
    List<GiangVien> findAllCoNguoiDung();
}
