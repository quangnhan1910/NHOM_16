package com.example.server.repository;

import com.example.server.model.CaThi;
import com.example.server.model.enums.TrangThaiCaThi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng ca_thi.
 */
@Repository
public interface CaThiRepository extends JpaRepository<CaThi, Integer> {

    long countByTrangThai(TrangThaiCaThi trangThai);

    @Query("SELECT DISTINCT ct FROM CaThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH dt.giangVien gv " +
           "JOIN FETCH gv.nguoiDung " +
           "JOIN FETCH dt.monHoc mh " +
           "WHERE gv.ma = :maGiangVien " +
           "AND ct.trangThai = :trangThai " +
           "ORDER BY ct.thoiGianBatDau ASC")
    List<CaThi> findByGiangVienMaAndTrangThai(
        @Param("maGiangVien") Integer maGiangVien,
        @Param("trangThai") TrangThaiCaThi trangThai);

    @Query("SELECT DISTINCT ct FROM CaThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH dt.giangVien gv " +
           "JOIN FETCH gv.nguoiDung " +
           "JOIN FETCH dt.monHoc mh " +
           "WHERE ct.trangThai = :trangThai " +
           "ORDER BY ct.thoiGianBatDau ASC")
    List<CaThi> findByTrangThai(@Param("trangThai") TrangThaiCaThi trangThai);

    @Query("SELECT DISTINCT ct FROM CaThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH dt.monHoc mh " +
           "JOIN FETCH dt.giangVien gv " +
           "JOIN FETCH gv.nguoiDung " +
           "WHERE ct.ma = :maCaThi")
    CaThi findByMaWithDetails(@Param("maCaThi") Integer maCaThi);

    @Query("SELECT DISTINCT ct FROM CaThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH dt.monHoc mh " +
           "JOIN FETCH dt.giangVien gv " +
           "JOIN FETCH gv.nguoiDung " +
           "WHERE gv.ma = :maGiangVien " +
           "AND ct.trangThai = :trangThai " +
           "ORDER BY ct.thoiGianBatDau DESC")
    List<CaThi> findByGiangVienMaAndTrangThaiSorted(
        @Param("maGiangVien") Integer maGiangVien,
        @Param("trangThai") TrangThaiCaThi trangThai);

    @Query("SELECT DISTINCT ct FROM CaThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH dt.monHoc mh " +
           "JOIN FETCH dt.giangVien gv " +
           "WHERE gv.ma = :maGiangVien " +
           "AND ct.trangThai IN :trangThais " +
           "ORDER BY ct.thoiGianBatDau DESC")
    List<CaThi> findByGiangVienMaAndTrangThaiIn(
        @Param("maGiangVien") Integer maGiangVien,
        @Param("trangThais") List<TrangThaiCaThi> trangThais);

    /**
     * Lấy trang ca thi có lọc theo trạng thái và khoa, kèm JOIN FETCH deThi và khoa.
     * Vì deThi và khoa đều là @ManyToOne (không sinh thêm hàng), an toàn để dùng với phân trang.
     * countQuery riêng biệt để không có FETCH trong câu đếm.
     */
    @Query(value = """
            SELECT ct FROM CaThi ct
            LEFT JOIN FETCH ct.deThi
            LEFT JOIN FETCH ct.khoa k
            WHERE (:trangThai IS NULL OR ct.trangThai = :trangThai)
              AND (:maKhoa IS NULL OR k.ma = :maKhoa)
            ORDER BY ct.ma DESC
            """,
           countQuery = """
            SELECT COUNT(ct) FROM CaThi ct
            LEFT JOIN ct.khoa k
            WHERE (:trangThai IS NULL OR ct.trangThai = :trangThai)
              AND (:maKhoa IS NULL OR k.ma = :maKhoa)
            """)
    Page<CaThi> timTheoBoLoc(
            @Param("trangThai") TrangThaiCaThi trangThai,
            @Param("maKhoa") Integer maKhoa,
            Pageable pageable);

    /**
     * Lấy chi tiết ca thi kèm deThi, monHoc của deThi, và khoa – dùng cho trang chi tiết.
     */
    @Query("""
            SELECT ct FROM CaThi ct
            LEFT JOIN FETCH ct.deThi dt
            LEFT JOIN FETCH dt.monHoc
            LEFT JOIN FETCH ct.khoa
            WHERE ct.ma = :ma
            """)
    Optional<CaThi> findChiTietByMa(@Param("ma") Integer ma);

    /**
     * Đếm số đăng ký thi theo nhóm ca thi – batch query tránh N+1.
     * Trả về mảng [maCaThi (Integer), soLuong (Long)].
     */
    @Query("""
            SELECT dk.caThi.ma, COUNT(dk)
            FROM DangKyThi dk
            WHERE dk.caThi.ma IN :maCaThis
            GROUP BY dk.caThi.ma
            """)
    List<Object[]> demSoLuongDangKyTheoNhom(@Param("maCaThis") List<Integer> maCaThis);

    /**
     * Đếm số bài thi đã nộp trong một ca thi.
     */
    @Query("SELECT COUNT(bt) FROM BaiThiSinhVien bt WHERE bt.caThi.ma = :maCaThi")
    int demSoBaiThiDaNop(@Param("maCaThi") Integer maCaThi);
}
