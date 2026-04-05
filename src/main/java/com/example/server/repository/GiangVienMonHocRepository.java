package com.example.server.repository;

import com.example.server.model.GiangVienMonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng giang_vien_mon_hoc.
 */
@Repository
public interface GiangVienMonHocRepository extends JpaRepository<GiangVienMonHoc, Integer> {

    @Query("SELECT gvmh FROM GiangVienMonHoc gvmh JOIN FETCH gvmh.monHoc WHERE gvmh.ma = :ma")
    Optional<GiangVienMonHoc> findByMaWithMonHoc(@Param("ma") Integer ma);

    /** findByMaWithMonHoc + tải danh sách sinh viên phân công và quan hệ hiển thị. */
    @Query("SELECT DISTINCT gvmh FROM GiangVienMonHoc gvmh " +
           "JOIN FETCH gvmh.monHoc " +
           "LEFT JOIN FETCH gvmh.sinhVienPhanCongs link " +
           "LEFT JOIN FETCH link.sinhVien sv " +
           "LEFT JOIN FETCH sv.nguoiDung " +
           "LEFT JOIN FETCH sv.chuyenNganh cn " +
           "LEFT JOIN FETCH cn.nganh " +
           "WHERE gvmh.ma = :ma")
    Optional<GiangVienMonHoc> findByMaWithMonHocVaSinhVienPhanCong(@Param("ma") Integer ma);

    @Query("SELECT gvmh FROM GiangVienMonHoc gvmh " +
           "JOIN FETCH gvmh.monHoc " +
           "WHERE gvmh.giangVien.ma = :maGiangVien")
    List<GiangVienMonHoc> findByGiangVienMa(@Param("maGiangVien") Integer maGiangVien);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GiangVienMonHoc g "
            + "WHERE g.giangVien.ma = :maGv AND g.monHoc.ma = :maMh")
    boolean existsByGiangVienMaAndMonHocMa(@Param("maGv") Integer maGv, @Param("maMh") Integer maMh);

    @Query("SELECT DISTINCT g FROM GiangVienMonHoc g " +
           "JOIN FETCH g.giangVien gv " +
           "JOIN FETCH gv.nguoiDung " +
           "JOIN FETCH g.monHoc " +
           "LEFT JOIN FETCH g.chuyenNganh " +
           "LEFT JOIN FETCH g.sinhVienPhanCongs link " +
           "LEFT JOIN FETCH link.sinhVien sv " +
           "LEFT JOIN FETCH sv.nguoiDung " +
           "ORDER BY g.ma DESC")
    List<GiangVienMonHoc> findAllChiTietPhanCong();

    /** Trùng khi cùng giảng viên + môn + chuyên ngành (một dòng phân công chứa nhiều SV). */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GiangVienMonHoc g " +
           "WHERE g.giangVien.ma = :maGv AND g.monHoc.ma = :maMh " +
           "AND ((:maCn IS NULL AND g.chuyenNganh IS NULL) " +
           "OR (g.chuyenNganh IS NOT NULL AND g.chuyenNganh.ma = :maCn))")
    boolean existsTrungPhanCong(
            @Param("maGv") Integer maGv,
            @Param("maMh") Integer maMh,
            @Param("maCn") Integer maCn);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GiangVienMonHoc g " +
           "WHERE g.ma <> :boQuaMa AND g.giangVien.ma = :maGv AND g.monHoc.ma = :maMh " +
           "AND ((:maCn IS NULL AND g.chuyenNganh IS NULL) " +
           "OR (g.chuyenNganh IS NOT NULL AND g.chuyenNganh.ma = :maCn))")
    boolean existsTrungPhanCongBoQuaMa(
            @Param("maGv") Integer maGv,
            @Param("maMh") Integer maMh,
            @Param("maCn") Integer maCn,
            @Param("boQuaMa") Integer boQuaMa);
}
