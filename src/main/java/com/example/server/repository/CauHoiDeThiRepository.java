package com.example.server.repository;

import com.example.server.model.CauHoiDeThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn bảng cau_hoi_de_thi.
 */
@Repository
public interface CauHoiDeThiRepository extends JpaRepository<CauHoiDeThi, Integer> {

    @Query("SELECT COUNT(c) FROM CauHoiDeThi c WHERE c.deThi.ma = :maDeThi")
    Integer demSoCauHoi(@Param("maDeThi") Integer maDeThi);

    @Query("SELECT c FROM CauHoiDeThi c " +
           "WHERE c.deThi.ma = :maDeThi " +
           "ORDER BY c.diem ASC")
    List<CauHoiDeThi> findByDeThiMaOrderByDiemAsc(@Param("maDeThi") Integer maDeThi);

    @Query("""
            SELECT DISTINCT c FROM CauHoiDeThi c
            JOIN FETCH c.cauHoi ch
            LEFT JOIN FETCH ch.luaChonCauHois lch
            WHERE c.deThi.ma = :maDeThi
            ORDER BY c.thuTuCauHoi ASC, c.ma ASC
            """)
    List<CauHoiDeThi> findByDeThiMaForClient(@Param("maDeThi") Integer maDeThi);
}
