package com.example.server.service;

import com.example.server.dto.TruongDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TruongService {

    /** Tạo trường đơn lẻ. */
    TruongDTO tao(TruongDTO dto, HttpServletRequest request);

    /** Tạo trường cùng nhiều khoa trong 1 lần. */
    List<TruongDTO> taoKemNhieuKhoa(TruongDTO truongDTO, List<String> tenKhoas, HttpServletRequest request);

    TruongDTO capNhat(Integer ma, TruongDTO dto, HttpServletRequest request);

    void xoa(Integer ma, HttpServletRequest request);

    TruongDTO timTheoMa(Integer ma);

    Page<TruongDTO> layDanhSachPhanTrang(String keyword, Pageable pageable);

    List<TruongDTO> layTatCa();
}
