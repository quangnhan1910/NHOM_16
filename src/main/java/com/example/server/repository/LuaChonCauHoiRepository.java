package com.example.server.repository;

import com.example.server.model.LuaChonCauHoi;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository truy vấn bảng lua_chon_cau_hoi.
 */
public interface LuaChonCauHoiRepository extends JpaRepository<LuaChonCauHoi, Integer> {
}
