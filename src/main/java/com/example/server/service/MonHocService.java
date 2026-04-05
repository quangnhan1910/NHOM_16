package com.example.server.service;

import com.example.server.dto.MonHocDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonHocService {

    /** Tạo mới môn học. */
    MonHocDTO tao(MonHocDTO dto, HttpServletRequest httpRequest);

    /** Cập nhật môn học. */
    MonHocDTO capNhat(Integer ma, MonHocDTO dto, HttpServletRequest httpRequest);

    /** Xóa môn học. */
    void xoa(Integer ma, HttpServletRequest httpRequest);

    /** Tìm theo mã. */
    MonHocDTO timTheoMa(Integer ma);

    /** Danh sách phân trang. */
    Page<MonHocDTO> layDanhSachPhanTrang(String keyword, Integer maKhoa, Integer soTinChi, Pageable pageable);

    /** Tất cả. */
    List<MonHocDTO> layTatCa();
}
