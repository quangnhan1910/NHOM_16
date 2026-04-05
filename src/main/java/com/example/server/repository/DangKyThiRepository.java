package com.example.server.repository;

import com.example.server.model.DangKyThi;
import com.example.server.model.MonHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng dang_ky_thi.
 */
@Repository
public interface DangKyThiRepository extends JpaRepository<DangKyThi, Integer> {

    /**
     * Lấy danh sách đăng ký thi của một ca thi, kèm JOIN FETCH sinh viên, người dùng, chuyên ngành.
     * Hỗ trợ lọc theo trạng thái check-in và tìm kiếm theo MSSV hoặc họ tên.
     */
    @Query(value = """
            SELECT dk FROM DangKyThi dk
            LEFT JOIN FETCH dk.sinhVien sv
            LEFT JOIN FETCH sv.nguoiDung nd
            LEFT JOIN FETCH sv.chuyenNganh cn
            WHERE dk.caThi.ma = :maCaThi
              AND (:daCheckIn IS NULL OR dk.daCheckIn = :daCheckIn)
              AND (:tuKhoa IS NULL
                   OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR sv.maSinhVien LIKE CONCAT('%', :tuKhoa, '%'))
            ORDER BY sv.maSinhVien ASC
            """,
           countQuery = """
            SELECT COUNT(dk) FROM DangKyThi dk
            LEFT JOIN dk.sinhVien sv
            LEFT JOIN sv.nguoiDung nd
            WHERE dk.caThi.ma = :maCaThi
              AND (:daCheckIn IS NULL OR dk.daCheckIn = :daCheckIn)
              AND (:tuKhoa IS NULL
                   OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR sv.maSinhVien LIKE CONCAT('%', :tuKhoa, '%'))
            """)
    Page<DangKyThi> timTheoBoLocThaoTac(
            @Param("maCaThi") Integer maCaThi,
            @Param("daCheckIn") Boolean daCheckIn,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable);

    /** Đếm tổng số sinh viên đăng ký trong một ca thi. */
    @Query("SELECT COUNT(dk) FROM DangKyThi dk WHERE dk.caThi.ma = :maCaThi")
    int demTong(@Param("maCaThi") Integer maCaThi);

    /** Đếm số sinh viên đã check-in trong một ca thi. */
    @Query("SELECT COUNT(dk) FROM DangKyThi dk WHERE dk.caThi.ma = :maCaThi AND dk.daCheckIn = true")
    int demSoCheckIn(@Param("maCaThi") Integer maCaThi);

    /** Kiểm tra sinh viên đã đăng ký ca thi chưa (tránh đăng ký trùng). */
    Optional<DangKyThi> findByCaThiMaAndSinhVienMa(Integer maCaThi, Integer maSinhVienMa);

    /**
     * Lấy toàn bộ đăng ký của một ca thi, kèm thông tin sinh viên và người dùng.
     * Dùng cho màn hình giám sát để vẫn hiển thị SV chưa vào làm bài.
     */
    @Query("""
            SELECT dk FROM DangKyThi dk
            LEFT JOIN FETCH dk.sinhVien sv
            LEFT JOIN FETCH sv.nguoiDung nd
            LEFT JOIN FETCH sv.chuyenNganh cn
            WHERE dk.caThi.ma = :maCaThi
            ORDER BY sv.maSinhVien ASC
            """)
    List<DangKyThi> findByCaThiMaWithDetails(@Param("maCaThi") Integer maCaThi);

    /**
     * Lấy danh sách đăng ký thi của một sinh viên, kèm JOIN FETCH ca thi, đề thi, khoa
     * để hiển thị trên dashboard sinh viên.
     */
    @Query("""
            SELECT dk FROM DangKyThi dk
            LEFT JOIN FETCH dk.caThi ct
            LEFT JOIN FETCH ct.deThi dt
            LEFT JOIN FETCH ct.khoa k
            WHERE dk.sinhVien.ma = :maSinhVienMa
            ORDER BY ct.thoiGianBatDau DESC
            """)
    List<DangKyThi> findBySinhVienMaWithDetails(@Param("maSinhVienMa") Integer maSinhVienMa);

    /**
     * Tải đầy đủ thông tin một đăng ký thi cho trang sảnh chờ.
     * JOIN FETCH toàn bộ chuỗi: DangKyThi → SinhVien → ChuyenNganh → Nganh → Khoa
     * và DangKyThi → CaThi → DeThi → MonHoc.
     */
    @Query("""
            SELECT dk FROM DangKyThi dk
            LEFT JOIN FETCH dk.sinhVien sv
            LEFT JOIN FETCH sv.nguoiDung nd
            LEFT JOIN FETCH sv.chuyenNganh cn
            LEFT JOIN FETCH cn.nganh ng
            LEFT JOIN FETCH ng.khoa kh
            LEFT JOIN FETCH dk.caThi ct
            LEFT JOIN FETCH ct.deThi dt
            LEFT JOIN FETCH dt.monHoc mh
            WHERE dk.ma = :maDangKy
            """)
    Optional<DangKyThi> findByMaForSanhCho(@Param("maDangKy") Integer maDangKy);

    @Query("SELECT COUNT(DISTINCT dk.sinhVien.ma) FROM DangKyThi dk " +
           "JOIN dk.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.monHoc.ma = :maMonHoc")
    Long demSoLuongSinhVienTheoMonHoc(@Param("maMonHoc") Integer maMonHoc);

    @Query("SELECT DISTINCT dt.monHoc FROM DangKyThi dk " +
           "JOIN dk.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dk.sinhVien.ma = :maSinhVien")
    List<MonHoc> findMonHocBySinhVienMa(@Param("maSinhVien") Integer maSinhVien);

    @Query("SELECT DISTINCT dk.sinhVien FROM DangKyThi dk " +
           "JOIN dk.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.monHoc.ma = :maMonHoc " +
           "ORDER BY dk.sinhVien.maSinhVien ASC")
    List<com.example.server.model.SinhVien> findSinhVienByMonHocMa(@Param("maMonHoc") Integer maMonHoc);

    @Query("SELECT CASE WHEN COUNT(dk) > 0 THEN true ELSE false END FROM DangKyThi dk " +
           "JOIN dk.caThi ct JOIN ct.deThi dt " +
           "WHERE dk.sinhVien.ma = :maSinhVien AND dt.monHoc.ma = :maMonHoc")
    boolean coDangKyThiChoSinhVienVaMonHoc(
            @Param("maSinhVien") Integer maSinhVien,
            @Param("maMonHoc") Integer maMonHoc);

    @Query("SELECT COUNT(dk) FROM DangKyThi dk WHERE dk.caThi.ma = :maCaThi")
    Integer demSoDangKy(@Param("maCaThi") Integer maCaThi);
}
