package com.example.server.repository;

import com.example.server.model.Nganh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn bảng nganh.
 */
@Repository
public interface NganhRepository extends JpaRepository<Nganh, Integer> {

    @Query("SELECT n FROM Nganh n WHERE " +
           "(:keyword IS NULL OR n.ten LIKE %:keyword%) " +
           "AND (:maKhoa IS NULL OR n.khoa.ma = :maKhoa)")
    Page<Nganh> searchNganh(@Param("keyword") String keyword,
                            @Param("maKhoa") Integer maKhoa,
                            Pageable pageable);

    List<Nganh> findByKhoaMa(Integer maKhoa);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Nganh n WHERE n.khoa.ma = :maKhoa")
    boolean coNganhThuocKhoa(@Param("maKhoa") Integer maKhoa);

    @Query("SELECT CASE WHEN COUNT(cn) > 0 THEN true ELSE false END FROM ChuyenNganh cn WHERE cn.nganh.ma = :maNganh")
    boolean coChuyenNganhThuocNganh(@Param("maNganh") Integer maNganh);
}
