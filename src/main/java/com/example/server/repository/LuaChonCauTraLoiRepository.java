package com.example.server.repository;

import com.example.server.model.LuaChonCauTraLoi;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository truy vấn bảng lua_chon_cau_tra_loi.
 */
public interface LuaChonCauTraLoiRepository extends JpaRepository<LuaChonCauTraLoi, Integer> {
}
