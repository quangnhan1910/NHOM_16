package com.example.server.service;

import com.example.server.dto.*;
import com.example.server.model.enums.VaiTro;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NguoiDungService {

    /** Tạo người dùng mới cùng profile theo vai trò. */
    NguoiDungDTO tao(TaoNguoiDungRequest request, HttpServletRequest httpRequest);

    /** Cập nhật người dùng và profile theo vai trò. */
    NguoiDungDTO capNhat(Integer ma, CapNhatNguoiDungRequest request, HttpServletRequest httpRequest);

    /** Xóa người dùng — chỉ xóa nếu không có ràng buộc nghiệp vụ. */
    void xoa(Integer ma, HttpServletRequest httpRequest);

    /** Khóa tài khoản. */
    NguoiDungDTO khoa(Integer ma, HttpServletRequest httpRequest);

    /** Mở khóa tài khoản. */
    NguoiDungDTO moKhoa(Integer ma, HttpServletRequest httpRequest);

    /** Lấy thông tin người dùng theo mã. */
    NguoiDungDTO timTheoMa(Integer ma);

    /** Lấy thông tin người dùng hiện tại đang đăng nhập. */
    NguoiDungDTO layNguoiDungHienTai();

    /** Đổi mật khẩu cho người dùng hiện tại. */
    void doiMatKhau(DoiMatKhauRequest request, HttpServletRequest httpRequest);

    /** Danh sách phân trang, lọc theo từ khóa, vai trò, trạng thái. */
    Page<NguoiDungDTO> layDanhSachPhanTrang(String keyword, VaiTro vaiTro, Boolean trangThai, Pageable pageable);

    /** Tất cả người dùng (không phân trang). */
    List<NguoiDungDTO> layTatCa();

    /** Thống kê người dùng. */
    ThongKeNguoiDungDTO thongKe();
}
