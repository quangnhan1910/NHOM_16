package com.example.server.repository;

import com.example.server.model.DeThi;
import com.example.server.model.enums.LoaiDeThi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository truy vấn bảng de_thi.
 */
@Repository
public interface DeThiRepository extends JpaRepository<DeThi, Integer> {

    /**
     * Tìm kiếm đề thi theo trạng thái xuất bản và từ khóa tên đề thi.
     * Nếu daXuatBan = null → lấy tất cả.
     * Nếu tuKhoa = null hoặc rỗng → không lọc theo tên.
     */
    @Query(value = "SELECT d FROM DeThi d JOIN FETCH d.monHoc " +
           "WHERE (:daXuatBan IS NULL OR d.daXuatBan = :daXuatBan) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(d.tenDeThi) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))",
           countQuery = "SELECT COUNT(d) FROM DeThi d " +
           "WHERE (:daXuatBan IS NULL OR d.daXuatBan = :daXuatBan) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(d.tenDeThi) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))")
    Page<DeThi> timKiem(@Param("daXuatBan") Boolean daXuatBan,
                        @Param("tuKhoa") String tuKhoa,
                        Pageable pageable);

    /**
     * Đếm số đề thi theo trạng thái xuất bản.
     */
    long countByDaXuatBan(Boolean daXuatBan);

    long countByLoaiDeThi(LoaiDeThi loaiDeThi);

    Page<DeThi> findByMonHocMa(Integer maMonHoc, Pageable pageable);

    Page<DeThi> findByLoaiDeThi(LoaiDeThi loaiDeThi, Pageable pageable);

    @Query("SELECT d FROM DeThi d WHERE " +
           "(:keyword IS NULL OR d.tenDeThi LIKE %:keyword%) " +
           "AND (:maMonHoc IS NULL OR d.monHoc.ma = :maMonHoc) " +
           "AND (:loaiDeThi IS NULL OR d.loaiDeThi = :loaiDeThi) " +
           "AND (:daXuatBan IS NULL OR d.daXuatBan = :daXuatBan)")
    Page<DeThi> searchDeThi(
            @Param("keyword") String keyword,
            @Param("maMonHoc") Integer maMonHoc,
            @Param("loaiDeThi") LoaiDeThi loaiDeThi,
            @Param("daXuatBan") Boolean daXuatBan,
            Pageable pageable);
}
