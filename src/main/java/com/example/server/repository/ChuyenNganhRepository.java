package com.example.server.repository;

import com.example.server.model.ChuyenNganh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn bảng chuyen_nganh.
 */
@Repository
public interface ChuyenNganhRepository extends JpaRepository<ChuyenNganh, Integer> {

    @Query("SELECT cn FROM ChuyenNganh cn WHERE " +
           "(:keyword IS NULL OR cn.ten LIKE %:keyword%) " +
           "AND (:maNganh IS NULL OR cn.nganh.ma = :maNganh)")
    Page<ChuyenNganh> searchChuyenNganh(@Param("keyword") String keyword,
                                         @Param("maNganh") Integer maNganh,
                                         Pageable pageable);

    List<ChuyenNganh> findByNganhMa(Integer maNganh);

    boolean existsByNganhMa(Integer maNganh);
}
