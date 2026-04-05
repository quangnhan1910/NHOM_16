package com.example.server.service;

import com.example.server.dto.*;
import com.example.server.exception.BadRequestException;
import com.example.server.exception.ResourceNotFoundException;
import com.example.server.model.*;
import com.example.server.model.enums.VaiTro;
import com.example.server.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NguoiDungServiceImpl implements NguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;
    private final QuanTriVienHeThongRepository quanTriVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final TruongRepository truongRepository;
    private final KhoaRepository khoaRepository;
    private final ChuyenNganhRepository chuyenNganhRepository;
    private final NhatKyService nhatKyService;
    private final PasswordEncoder passwordEncoder;

    public NguoiDungServiceImpl(
            NguoiDungRepository nguoiDungRepository,
            QuanTriVienHeThongRepository quanTriVienRepository,
            GiangVienRepository giangVienRepository,
            SinhVienRepository sinhVienRepository,
            TruongRepository truongRepository,
            KhoaRepository khoaRepository,
            ChuyenNganhRepository chuyenNganhRepository,
            NhatKyService nhatKyService,
            PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.quanTriVienRepository = quanTriVienRepository;
        this.giangVienRepository = giangVienRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.truongRepository = truongRepository;
        this.khoaRepository = khoaRepository;
        this.chuyenNganhRepository = chuyenNganhRepository;
        this.nhatKyService = nhatKyService;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== TAO ==========

    @Override
    public NguoiDungDTO tao(TaoNguoiDungRequest request, HttpServletRequest httpRequest) {
        if (nguoiDungRepository.existsByThuDienTu(request.getThuDienTu())) {
            throw new BadRequestException("Email '" + request.getThuDienTu() + "' đã tồn tại.");
        }

        // --- Tạo NguoiDung ---
        NguoiDung nguoiDung = NguoiDung.builder()
                .thuDienTu(request.getThuDienTu())
                .matKhauMaHoa(passwordEncoder.encode(request.getMatKhau()))
                .hoTen(request.getHoTen())
                .vaiTro(request.getVaiTro())
                .trangThaiHoatDong(true)
                .taoLuc(Instant.now())
                .build();

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);

        // --- Tạo profile theo vai trò ---
        TaoProfileTheoVaiTro(request.getVaiTro(), saved, request);

        nhatKyService.ghiNhatKy(HanhDong.THEM_NGUOI_DUNG, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    private void TaoProfileTheoVaiTro(VaiTro vaiTro, NguoiDung nguoiDung, TaoNguoiDungRequest request) {
        switch (vaiTro) {
            case QUAN_TRI_VIEN -> {
                if (request.getMaTruong() == null) {
                    throw new BadRequestException("Mã trường không được để trống khi tạo tài khoản quản trị viên.");
                }
                Truong truong = truongRepository.findById(request.getMaTruong())
                        .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", request.getMaTruong()));
                QuanTriVienHeThong qtv = QuanTriVienHeThong.builder()
                        .nguoiDung(nguoiDung)
                        .truong(truong)
                        .build();
                quanTriVienRepository.save(qtv);
            }
            case GIANG_VIEN -> {
                if (request.getMaKhoa() == null) {
                    throw new BadRequestException("Mã khoa không được để trống khi tạo tài khoản giảng viên.");
                }
                if (request.getMaNhanVien() == null || request.getMaNhanVien().isBlank()) {
                    throw new BadRequestException("Mã nhân viên không được để trống khi tạo tài khoản giảng viên.");
                }
                if (giangVienRepository.existsByMaNhanVien(request.getMaNhanVien())) {
                    throw new BadRequestException("Mã nhân viên '" + request.getMaNhanVien() + "' đã tồn tại.");
                }
                Khoa khoa = khoaRepository.findById(request.getMaKhoa())
                        .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", request.getMaKhoa()));
                GiangVien gv = GiangVien.builder()
                        .nguoiDung(nguoiDung)
                        .khoa(khoa)
                        .maNhanVien(request.getMaNhanVien())
                        .build();
                giangVienRepository.save(gv);
            }
            case SINH_VIEN -> {
                if (request.getMaChuyenNganh() == null) {
                    throw new BadRequestException("Mã chuyên ngành không được để trống khi tạo tài khoản sinh viên.");
                }
                if (request.getMaSinhVien() == null || request.getMaSinhVien().isBlank()) {
                    throw new BadRequestException("Mã sinh viên không được để trống khi tạo tài khoản sinh viên.");
                }
                if (request.getBacDaoTao() == null) {
                    throw new BadRequestException("Bậc đào tạo không được để trống khi tạo tài khoản sinh viên.");
                }
                if (sinhVienRepository.existsByMaSinhVien(request.getMaSinhVien())) {
                    throw new BadRequestException("Mã sinh viên '" + request.getMaSinhVien() + "' đã tồn tại.");
                }
                ChuyenNganh cn = chuyenNganhRepository.findById(request.getMaChuyenNganh())
                        .orElseThrow(() -> new ResourceNotFoundException("ChuyenNganh", "ma", request.getMaChuyenNganh()));
                SinhVien sv = SinhVien.builder()
                        .nguoiDung(nguoiDung)
                        .chuyenNganh(cn)
                        .maSinhVien(request.getMaSinhVien())
                        .bacDaoTao(request.getBacDaoTao())
                        .build();
                sinhVienRepository.save(sv);
            }
        }
    }

    // ========== CAP NHAT ==========

    @Override
    public NguoiDungDTO capNhat(Integer ma, CapNhatNguoiDungRequest request, HttpServletRequest httpRequest) {
        NguoiDung entity = nguoiDungRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("NguoiDung", "ma", ma));

        // Check trùng email — bỏ qua chính bản ghi hiện tại
        if (!entity.getThuDienTu().equals(request.getThuDienTu())
                && nguoiDungRepository.existsByThuDienTu(request.getThuDienTu())) {
            throw new BadRequestException("Email '" + request.getThuDienTu() + "' đã tồn tại.");
        }

        entity.setThuDienTu(request.getThuDienTu());
        entity.setHoTen(request.getHoTen());
        entity.setVaiTro(request.getVaiTro());
        entity.setCapNhatLuc(Instant.now());

        // Cập nhật profile theo vai trò mới
        CapNhatProfileTheoVaiTro(entity, request);

        NguoiDung saved = nguoiDungRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_NGUOI_DUNG, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    private void CapNhatProfileTheoVaiTro(NguoiDung nguoiDung, CapNhatNguoiDungRequest request) {
        VaiTro vaiTro = request.getVaiTro();
        switch (vaiTro) {
            case QUAN_TRI_VIEN -> {
                if (request.getMaTruong() == null) {
                    throw new BadRequestException("Mã trường không được để trống.");
                }
                Truong truong = truongRepository.findById(request.getMaTruong())
                        .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", request.getMaTruong()));
                quanTriVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                        .ifPresentOrElse(
                                qtv -> qtv.setTruong(truong),
                                () -> {
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.GIANG_VIEN);
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.SINH_VIEN);
                                    QuanTriVienHeThong qtv = QuanTriVienHeThong.builder()
                                            .nguoiDung(nguoiDung)
                                            .truong(truong)
                                            .build();
                                    quanTriVienRepository.save(qtv);
                                }
                        );
            }
            case GIANG_VIEN -> {
                if (request.getMaKhoa() == null) {
                    throw new BadRequestException("Mã khoa không được để trống.");
                }
                if (request.getMaNhanVien() == null || request.getMaNhanVien().isBlank()) {
                    throw new BadRequestException("Mã nhân viên không được để trống.");
                }
                Khoa khoa = khoaRepository.findById(request.getMaKhoa())
                        .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", request.getMaKhoa()));
                giangVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                        .ifPresentOrElse(
                                gv -> {
                                    if (!gv.getMaNhanVien().equals(request.getMaNhanVien())
                                            && giangVienRepository.existsByMaNhanVien(request.getMaNhanVien())) {
                                        throw new BadRequestException("Mã nhân viên '" + request.getMaNhanVien() + "' đã tồn tại.");
                                    }
                                    gv.setKhoa(khoa);
                                    gv.setMaNhanVien(request.getMaNhanVien());
                                },
                                () -> {
                                    if (giangVienRepository.existsByMaNhanVien(request.getMaNhanVien())) {
                                        throw new BadRequestException("Mã nhân viên '" + request.getMaNhanVien() + "' đã tồn tại.");
                                    }
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.QUAN_TRI_VIEN);
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.SINH_VIEN);
                                    GiangVien gv = GiangVien.builder()
                                            .nguoiDung(nguoiDung)
                                            .khoa(khoa)
                                            .maNhanVien(request.getMaNhanVien())
                                            .build();
                                    giangVienRepository.save(gv);
                                }
                        );
            }
            case SINH_VIEN -> {
                if (request.getMaChuyenNganh() == null) {
                    throw new BadRequestException("Mã chuyên ngành không được để trống.");
                }
                if (request.getMaSinhVien() == null || request.getMaSinhVien().isBlank()) {
                    throw new BadRequestException("Mã sinh viên không được để trống.");
                }
                if (request.getBacDaoTao() == null) {
                    throw new BadRequestException("Bậc đào tạo không được để trống.");
                }
                ChuyenNganh cn = chuyenNganhRepository.findById(request.getMaChuyenNganh())
                        .orElseThrow(() -> new ResourceNotFoundException("ChuyenNganh", "ma", request.getMaChuyenNganh()));
                sinhVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                        .ifPresentOrElse(
                                sv -> {
                                    if (!sv.getMaSinhVien().equals(request.getMaSinhVien())
                                            && sinhVienRepository.existsByMaSinhVien(request.getMaSinhVien())) {
                                        throw new BadRequestException("Mã sinh viên '" + request.getMaSinhVien() + "' đã tồn tại.");
                                    }
                                    sv.setChuyenNganh(cn);
                                    sv.setMaSinhVien(request.getMaSinhVien());
                                    sv.setBacDaoTao(request.getBacDaoTao());
                                },
                                () -> {
                                    if (sinhVienRepository.existsByMaSinhVien(request.getMaSinhVien())) {
                                        throw new BadRequestException("Mã sinh viên '" + request.getMaSinhVien() + "' đã tồn tại.");
                                    }
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.QUAN_TRI_VIEN);
                                    xoaProfileCuaVaiTroKhac(nguoiDung, VaiTro.GIANG_VIEN);
                                    SinhVien sv = SinhVien.builder()
                                            .nguoiDung(nguoiDung)
                                            .chuyenNganh(cn)
                                            .maSinhVien(request.getMaSinhVien())
                                            .bacDaoTao(request.getBacDaoTao())
                                            .build();
                                    sinhVienRepository.save(sv);
                                }
                        );
            }
        }
    }

    /**
     * Xóa profile của vai trò khác (nếu tồn tại) khi đổi sang vai trò mới.
     * Đảm bảo mỗi NguoiDung chỉ có tối đa một profile tại một thời điểm.
     */
    private void xoaProfileCuaVaiTroKhac(NguoiDung nguoiDung, VaiTro vaiTro) {
        switch (vaiTro) {
            case QUAN_TRI_VIEN -> quanTriVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                    .ifPresent(quanTriVienRepository::delete);
            case GIANG_VIEN -> giangVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                    .ifPresent(giangVienRepository::delete);
            case SINH_VIEN -> sinhVienRepository.findByNguoiDungMa(nguoiDung.getMa())
                    .ifPresent(sinhVienRepository::delete);
        }
    }

    // ========== KHOA ==========

    @Override
    public NguoiDungDTO khoa(Integer ma, HttpServletRequest httpRequest) {
        NguoiDung entity = nguoiDungRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("NguoiDung", "ma", ma));

        entity.setTrangThaiHoatDong(false);
        entity.setCapNhatLuc(Instant.now());
        NguoiDung saved = nguoiDungRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.KHOA_TAI_KHOAN, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    // ========== MO KHOA ==========

    @Override
    public NguoiDungDTO moKhoa(Integer ma, HttpServletRequest httpRequest) {
        NguoiDung entity = nguoiDungRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("NguoiDung", "ma", ma));

        entity.setTrangThaiHoatDong(true);
        entity.setCapNhatLuc(Instant.now());
        NguoiDung saved = nguoiDungRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.MO_TAI_KHOAN, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    // ========== XOA ==========

    @Override
    public void xoa(Integer ma, HttpServletRequest httpRequest) {
        NguoiDung entity = nguoiDungRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("NguoiDung", "ma", ma));

        // Lấy người dùng hiện tại để kiểm tra quyền
        NguoiDung currentUser = nhatKyService.layNguoiDungHienTai();
        boolean isAdmin = currentUser != null && currentUser.getVaiTro() == VaiTro.QUAN_TRI_VIEN;

        boolean coQuanTriVien = quanTriVienRepository.findByNguoiDungMa(ma).isPresent();
        boolean coGiangVien = giangVienRepository.findByNguoiDungMa(ma).isPresent();
        boolean coSinhVien = sinhVienRepository.findByNguoiDungMa(ma).isPresent();

        if (!coQuanTriVien && !coGiangVien && !coSinhVien) {
            // Không có profile nghiệp vụ → xóa trực tiếp
            nguoiDungRepository.deleteById(ma);
            nhatKyService.ghiNhatKy(HanhDong.XOA_NGUOI_DUNG, null, ma, httpRequest);
        } else if (isAdmin) {
            // Admin có quyền xóa hoàn toàn, xóa cả profile trước
            if (coQuanTriVien) {
                quanTriVienRepository.findByNguoiDungMa(ma).ifPresent(quanTriVienRepository::delete);
            }
            if (coGiangVien) {
                giangVienRepository.findByNguoiDungMa(ma).ifPresent(giangVienRepository::delete);
            }
            if (coSinhVien) {
                sinhVienRepository.findByNguoiDungMa(ma).ifPresent(sinhVienRepository::delete);
            }
            // Sau khi xóa profile, xóa người dùng
            nguoiDungRepository.deleteById(ma);
            nhatKyService.ghiNhatKy(HanhDong.XOA_NGUOI_DUNG, null, ma, httpRequest);
        } else {
            // Không phải Admin và có profile → không xóa, chỉ khóa tài khoản thay thế
            if (Boolean.TRUE.equals(entity.getTrangThaiHoatDong())) {
                entity.setTrangThaiHoatDong(false);
                entity.setCapNhatLuc(Instant.now());
                nguoiDungRepository.save(entity);
                nhatKyService.ghiNhatKy(HanhDong.KHOA_TAI_KHOAN, null, ma, httpRequest);
            }
            throw new BadRequestException(
                    "Không thể xóa tài khoản này vì có ràng buộc nghiệp vụ. "
                    + "Tài khoản đã được tự động khóa thay vì xóa.");
        }
    }

    // ========== DOC ==========

    @Override
    @Transactional(readOnly = true)
    public NguoiDungDTO timTheoMa(Integer ma) {
        NguoiDung entity = nguoiDungRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("NguoiDung", "ma", ma));
        return toDTO(entity);
    }

    // ========== CURRENT USER ==========

    @Override
    @Transactional(readOnly = true)
    public NguoiDungDTO layNguoiDungHienTai() {
        NguoiDung nguoiDung = nhatKyService.layNguoiDungHienTai();
        if (nguoiDung == null) {
            throw new BadRequestException("Không xác định được người dùng hiện tại.");
        }
        return toDTO(nguoiDung);
    }

    // ========== DOI MAT KHAU ==========

    @Override
    public void doiMatKhau(DoiMatKhauRequest request, HttpServletRequest httpRequest) {
        NguoiDung nguoiDung = nhatKyService.layNguoiDungHienTai();
        if (nguoiDung == null) {
            throw new BadRequestException("Không xác định được người dùng hiện tại.");
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getMatKhauHienTai(), nguoiDung.getMatKhauMaHoa())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng.");
        }

        // Kiểm tra mật khẩu mới khớp xác nhận
        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
        }

        // Kiểm tra mật khẩu mới không trùng mật khẩu hiện tại
        if (passwordEncoder.matches(request.getMatKhauMoi(), nguoiDung.getMatKhauMaHoa())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
        }

        // Cập nhật mật khẩu mới
        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(request.getMatKhauMoi()));
        nguoiDung.setCapNhatLuc(Instant.now());
        nguoiDungRepository.save(nguoiDung);

        nhatKyService.ghiNhatKy(HanhDong.DOI_MAT_KHAU, null, nguoiDung.getMa(), httpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NguoiDungDTO> layDanhSachPhanTrang(String keyword, VaiTro vaiTro, Boolean trangThai, Pageable pageable) {
        return nguoiDungRepository.searchNguoiDung(keyword, vaiTro, trangThai, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NguoiDungDTO> layTatCa() {
        return nguoiDungRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // ========== THONG KE ==========

    @Override
    @Transactional(readOnly = true)
    public ThongKeNguoiDungDTO thongKe() {
        return ThongKeNguoiDungDTO.builder()
                .tongNguoiDung(nguoiDungRepository.count())
                .tongQuanTriVien(nguoiDungRepository.countByVaiTro(VaiTro.QUAN_TRI_VIEN))
                .tongGiangVien(nguoiDungRepository.countByVaiTro(VaiTro.GIANG_VIEN))
                .tongSinhVien(nguoiDungRepository.countByVaiTro(VaiTro.SINH_VIEN))
                .tongDangHoatDong(nguoiDungRepository.countByTrangThaiHoatDong(true))
                .tongBiKhoa(nguoiDungRepository.countByTrangThaiHoatDong(false))
                .build();
    }

    // ========== TO DTO ==========

    private NguoiDungDTO toDTO(NguoiDung entity) {
        NguoiDungDTO.NguoiDungDTOBuilder builder = NguoiDungDTO.builder()
                .ma(entity.getMa())
                .thuDienTu(entity.getThuDienTu())
                .hoTen(entity.getHoTen())
                .vaiTro(entity.getVaiTro())
                .trangThaiHoatDong(entity.getTrangThaiHoatDong())
                .taoLuc(entity.getTaoLuc())
                .capNhatLuc(entity.getCapNhatLuc());

        // Load profile data dựa trên vai trò
        Integer maNguoiDung = entity.getMa();
        quanTriVienRepository.findByNguoiDungMa(maNguoiDung)
                .ifPresent(qtv -> builder.maTruong(qtv.getTruong().getMa()));
        giangVienRepository.findByNguoiDungMa(maNguoiDung)
                .ifPresent(gv -> {
                    builder.maKhoa(gv.getKhoa().getMa());
                    builder.maNhanVien(gv.getMaNhanVien());
                });
        sinhVienRepository.findByNguoiDungMa(maNguoiDung)
                .ifPresent(sv -> {
                    builder.maChuyenNganh(sv.getChuyenNganh().getMa());
                    builder.maSinhVien(sv.getMaSinhVien());
                    builder.bacDaoTao(sv.getBacDaoTao());
                });

        return builder.build();
    }
}
