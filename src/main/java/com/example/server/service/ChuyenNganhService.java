package com.example.server.service;

import com.example.server.dto.ChuyenNganhDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service quản lý chuyên ngành.
 */
public interface ChuyenNganhService {

    ChuyenNganhDTO tao(ChuyenNganhDTO dto, HttpServletRequest request);

    /** Tạo 1 Chuyên ngành và nhiều Chuyên ngành cùng lúc. */
    List<ChuyenNganhDTO> taoKemNhieuChuyenNganh(Integer maNganh, List<String> tenChuyenNganhs, HttpServletRequest request);

    ChuyenNganhDTO capNhat(Integer ma, ChuyenNganhDTO dto, HttpServletRequest request);

    void xoa(Integer ma, HttpServletRequest request);

    ChuyenNganhDTO timTheoMa(Integer ma);

    Page<ChuyenNganhDTO> layDanhSachPhanTrang(String keyword, Integer maNganh, Pageable pageable);

    List<ChuyenNganhDTO> layTatCa();

    List<ChuyenNganhDTO> layTheoNganh(Integer maNganh);
}
