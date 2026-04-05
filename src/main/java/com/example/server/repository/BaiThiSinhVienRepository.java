package com.example.server.repository;

import com.example.server.model.BaiThiSinhVien;
import com.example.server.model.enums.TrangThaiBaiThi;
import com.example.server.model.enums.TrangThaiCaThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn bảng bai_thi_sinh_vien.
 */
@Repository
public interface BaiThiSinhVienRepository extends JpaRepository<BaiThiSinhVien, Integer> {

    @Query("SELECT bts FROM BaiThiSinhVien bts " +
           "JOIN FETCH bts.caThi ct " +
           "JOIN FETCH ct.deThi dt " +
           "JOIN FETCH bts.sinhVien sv " +
           "JOIN FETCH sv.nguoiDung nd " +
           "LEFT JOIN FETCH sv.chuyenNganh cn " +
           "LEFT JOIN FETCH cn.nganh n " +
           "WHERE bts.caThi.ma = :maCaThi " +
           "ORDER BY sv.maSinhVien ASC")
    List<BaiThiSinhVien> findByCaThiMaWithDetails(@Param("maCaThi") Integer maCaThi);

    @Query("""
           SELECT bts FROM BaiThiSinhVien bts
           JOIN FETCH bts.caThi ct
           JOIN FETCH ct.deThi dt
           LEFT JOIN FETCH dt.giangVien gv
           JOIN FETCH bts.sinhVien sv
           JOIN FETCH sv.nguoiDung nd
           LEFT JOIN FETCH sv.chuyenNganh cn
           WHERE bts.ma = :maBaiThi
           """)
    Optional<BaiThiSinhVien> findByMaForTheoDoi(@Param("maBaiThi") Integer maBaiThi);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts WHERE bts.caThi.ma = :maCaThi")
    Integer demTongBaiThi(@Param("maCaThi") Integer maCaThi);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "WHERE bts.caThi.ma = :maCaThi AND bts.trangThai = :trangThai")
    Integer demBaiThiTheoTrangThai(@Param("maCaThi") Integer maCaThi,
                                   @Param("trangThai") TrangThaiBaiThi trangThai);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.giangVien.ma = :maGiangVien " +
           "AND ct.trangThai = com.example.server.model.enums.TrangThaiCaThi.DANG_DIEN_RA")
    Integer demTongSinhVienDangThiCuaGiangVien(@Param("maGiangVien") Integer maGiangVien);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "WHERE ct.ma = :maCaThi AND bts.trangThai IN :trangThais")
    Integer demBaiThiDaNopTheoCaThi(@Param("maCaThi") Integer maCaThi,
                                     @Param("trangThais") List<TrangThaiBaiThi> trangThais);

    @Query("""
           SELECT bts FROM BaiThiSinhVien bts
           WHERE bts.caThi.ma = :maCaThi AND bts.sinhVien.ma = :maSinhVien
           ORDER BY bts.taoLuc DESC, bts.ma DESC
           """)
    List<BaiThiSinhVien> findByCaThiMaAndSinhVienMaOrderByMoiNhat(
            @Param("maCaThi") Integer maCaThi,
            @Param("maSinhVien") Integer maSinhVien);

    boolean existsByCaThiMaAndSinhVienMaAndTrangThaiIn(
            Integer maCaThi,
            Integer maSinhVien,
            List<TrangThaiBaiThi> trangThais);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.giangVien.ma = :maGiangVien " +
           "AND bts.trangThai IN :trangThaisBaiThi " +
           "AND ct.trangThai IN :trangThaisCaThi")
    Integer demTongBaiThiGiangVien(@Param("maGiangVien") Integer maGiangVien,
                                   @Param("trangThaisBaiThi") List<TrangThaiBaiThi> trangThaisBaiThi,
                                   @Param("trangThaisCaThi") List<TrangThaiCaThi> trangThaisCaThi);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.giangVien.ma = :maGiangVien " +
           "AND bts.trangThai IN :trangThaisBaiThi " +
           "AND ct.trangThai IN :trangThaisCaThi " +
           "AND ((:dat = true AND bts.tongDiem IS NOT NULL AND bts.tongDiem >= COALESCE(dt.diemDat, :nguongDat)) OR " +
           "(:dat = false AND (bts.tongDiem IS NULL OR bts.tongDiem < COALESCE(dt.diemDat, :nguongDat))))")
    Integer demBaiThiGiangVienTheoKetQuaDat(@Param("maGiangVien") Integer maGiangVien,
                                            @Param("trangThaisBaiThi") List<TrangThaiBaiThi> trangThaisBaiThi,
                                            @Param("trangThaisCaThi") List<TrangThaiCaThi> trangThaisCaThi,
                                            @Param("dat") boolean dat,
                                            @Param("nguongDat") BigDecimal nguongDat);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.giangVien.ma = :maGiangVien " +
           "AND bts.trangThai = :trangThai " +
           "AND ct.trangThai IN :trangThaisCaThi")
    Integer demBaiThiGiangVienTheoTrangThai(@Param("maGiangVien") Integer maGiangVien,
                                            @Param("trangThai") TrangThaiBaiThi trangThai,
                                            @Param("trangThaisCaThi") List<TrangThaiCaThi> trangThaisCaThi);

    @Query("SELECT AVG(bts.tongDiem) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE dt.giangVien.ma = :maGiangVien " +
           "AND bts.trangThai IN :trangThaisBaiThi " +
           "AND ct.trangThai IN :trangThaisCaThi " +
           "AND bts.tongDiem IS NOT NULL")
    Double tinhDiemTrungBinhGiangVien(@Param("maGiangVien") Integer maGiangVien,
                                     @Param("trangThaisBaiThi") List<TrangThaiBaiThi> trangThaisBaiThi,
                                     @Param("trangThaisCaThi") List<TrangThaiCaThi> trangThaisCaThi);

    @Query("SELECT AVG(bts.tongDiem) FROM BaiThiSinhVien bts " +
           "WHERE bts.caThi.ma = :maCaThi " +
           "AND bts.trangThai IN :trangThais " +
           "AND bts.tongDiem IS NOT NULL")
    Double tinhDiemTrungBinhTheoCaThi(@Param("maCaThi") Integer maCaThi,
                                     @Param("trangThais") List<TrangThaiBaiThi> trangThais);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "JOIN ct.deThi dt " +
           "WHERE bts.caThi.ma = :maCaThi " +
           "AND bts.trangThai IN :trangThais " +
           "AND ((:dat = true AND bts.tongDiem IS NOT NULL AND bts.tongDiem >= COALESCE(dt.diemDat, :nguongDat)) OR " +
           "(:dat = false AND (bts.tongDiem IS NULL OR bts.tongDiem < COALESCE(dt.diemDat, :nguongDat))))")
    Integer demBaiThiDatTheoCaThi(@Param("maCaThi") Integer maCaThi,
                                 @Param("dat") boolean dat,
                                 @Param("trangThais") List<TrangThaiBaiThi> trangThais,
                                 @Param("nguongDat") BigDecimal nguongDat);

    @Query("SELECT COUNT(bts) FROM BaiThiSinhVien bts " +
           "JOIN bts.caThi ct " +
           "WHERE ct.deThi.giangVien.ma = :maGiangVien " +
           "AND bts.trangThai IN :trangThais")
    Integer demTongSinhVienDaNopGiangVien(@Param("maGiangVien") Integer maGiangVien,
                                          @Param("trangThais") List<TrangThaiBaiThi> trangThais);
}
