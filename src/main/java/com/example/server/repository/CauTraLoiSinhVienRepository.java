package com.example.server.repository;

import com.example.server.model.CauTraLoiSinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn bảng cau_tra_loi_sinh_vien.
 */
@Repository
public interface CauTraLoiSinhVienRepository extends JpaRepository<CauTraLoiSinhVien, Integer> {

    @Query("SELECT COUNT(c) FROM CauTraLoiSinhVien c WHERE c.baiThiSinhVien.ma = :maBaiThi")
    Integer demSoCauDaTraLoi(@Param("maBaiThi") Integer maBaiThi);

    @Query("""
           SELECT DISTINCT c FROM CauTraLoiSinhVien c
           JOIN FETCH c.cauHoiDeThi chdt
           JOIN FETCH chdt.cauHoi ch
           LEFT JOIN FETCH c.luaChonCauTraLois lctl
           LEFT JOIN FETCH lctl.luaChon lc
           WHERE c.baiThiSinhVien.ma = :maBaiThi
           """)
    List<CauTraLoiSinhVien> findChiTietByBaiThiMa(@Param("maBaiThi") Integer maBaiThi);

    @Query("SELECT COUNT(c) FROM CauTraLoiSinhVien c " +
           "WHERE c.cauHoiDeThi.ma = :maCauHoiDeThi " +
           "AND c.laDapAnDung = true")
    Integer demSoDapAnDungTheoCauHoi(@Param("maCauHoiDeThi") Integer maCauHoiDeThi);

    @Query("SELECT COUNT(DISTINCT c.baiThiSinhVien.ma) FROM CauTraLoiSinhVien c " +
           "WHERE c.cauHoiDeThi.ma = :maCauHoiDeThi " +
           "AND c.laDapAnDung = true")
    Integer demSoSinhVienTraLoiDungTheoCauHoi(@Param("maCauHoiDeThi") Integer maCauHoiDeThi);

    @Query("SELECT COUNT(DISTINCT c.baiThiSinhVien.ma) FROM CauTraLoiSinhVien c " +
           "WHERE c.cauHoiDeThi.ma = :maCauHoiDeThi")
    Integer demTongSoSinhVienTraLoiTheoCauHoi(@Param("maCauHoiDeThi") Integer maCauHoiDeThi);
}
