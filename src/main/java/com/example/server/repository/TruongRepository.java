package com.example.server.repository;

import com.example.server.model.Truong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TruongRepository extends JpaRepository<Truong, Integer> {

    @Query("SELECT t FROM Truong t WHERE " +
           "(:keyword IS NULL OR t.ten LIKE %:keyword% OR t.maDinhDanh LIKE %:keyword%)")
    Page<Truong> searchTruong(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByMaDinhDanh(String maDinhDanh);
}
