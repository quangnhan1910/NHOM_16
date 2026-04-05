package com.example.server.service;

import com.example.server.dto.KhoaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service quản lý khoa.
 */
public interface KhoaService {

    KhoaDTO tao(KhoaDTO dto, HttpServletRequest request);

    KhoaDTO capNhat(Integer ma, KhoaDTO dto, HttpServletRequest request);

    void xoa(Integer ma, HttpServletRequest request);

    KhoaDTO timTheoMa(Integer ma);

    Page<KhoaDTO> layDanhSachPhanTrang(String keyword, Pageable pageable);

    List<KhoaDTO> layTatCa();

    List<KhoaDTO> layTheoTruong(Integer maTruong);
}
