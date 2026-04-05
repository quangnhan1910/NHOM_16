package com.example.server.service;

import com.example.server.dto.NganhDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service quản lý ngành.
 */
public interface NganhService {

    NganhDTO tao(NganhDTO dto, HttpServletRequest request);

    /** Tạo nhiều ngành cho 1 khoa đã có (Form 2: Thêm Ngành). */
    java.util.List<NganhDTO> taoNhieuNganh(Integer maKhoa, java.util.List<String> tenNganhs, HttpServletRequest request);

    NganhDTO capNhat(Integer ma, NganhDTO dto, HttpServletRequest request);

    void xoa(Integer ma, HttpServletRequest request);

    NganhDTO timTheoMa(Integer ma);

    org.springframework.data.domain.Page<NganhDTO> layDanhSachPhanTrang(String keyword, Integer maKhoa, org.springframework.data.domain.Pageable pageable);

    java.util.List<NganhDTO> layTatCa();

    java.util.List<NganhDTO> layTheoKhoa(Integer maKhoa);
}
