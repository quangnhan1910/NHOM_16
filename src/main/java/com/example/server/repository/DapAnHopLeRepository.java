package com.example.server.repository;

import com.example.server.model.DapAnHopLe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository truy vấn bảng dap_an_hop_le.
 */
public interface DapAnHopLeRepository extends JpaRepository<DapAnHopLe, Integer> {
}
