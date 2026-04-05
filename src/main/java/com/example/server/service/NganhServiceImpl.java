package com.example.server.service;

import com.example.server.dto.NganhDTO;
import com.example.server.exception.BadRequestException;
import com.example.server.exception.ResourceNotFoundException;
import com.example.server.model.Khoa;
import com.example.server.model.Nganh;
import com.example.server.repository.KhoaRepository;
import com.example.server.repository.NganhRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NganhServiceImpl implements NganhService {

    private final NganhRepository nganhRepository;
    private final KhoaRepository khoaRepository;
    private final NhatKyService nhatKyService;

    public NganhServiceImpl(NganhRepository nganhRepository,
                            KhoaRepository khoaRepository,
                            NhatKyService nhatKyService) {
        this.nganhRepository = nganhRepository;
        this.khoaRepository = khoaRepository;
        this.nhatKyService = nhatKyService;
    }

    @Override
    public NganhDTO tao(NganhDTO dto, HttpServletRequest request) {
        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", dto.getMaKhoa()));

        Nganh entity = Nganh.builder()
                .khoa(khoa)
                .ten(dto.getTen())
                .taoLuc(Instant.now())
                .build();

        Nganh saved = nganhRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.THEM_NGANH, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public List<NganhDTO> taoNhieuNganh(Integer maKhoa, List<String> tenNganhs, HttpServletRequest request) {
        Khoa khoa = khoaRepository.findById(maKhoa)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", maKhoa));
        List<NganhDTO> result = new ArrayList<>();
        if (tenNganhs != null) {
            for (String tenNganh : tenNganhs) {
                if (tenNganh != null && !tenNganh.trim().isEmpty()) {
                    Nganh nganh = Nganh.builder()
                            .khoa(khoa)
                            .ten(tenNganh.trim())
                            .taoLuc(Instant.now())
                            .build();
                    nganh = nganhRepository.save(nganh);
                    nhatKyService.ghiNhatKy(HanhDong.THEM_NGANH, null, nganh.getMa(), request);
                    result.add(toDTO(nganh));
                }
            }
        }
        return result;
    }

    @Override
    public NganhDTO capNhat(Integer ma, NganhDTO dto, HttpServletRequest request) {
        Nganh entity = nganhRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Nganh", "ma", ma));

        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", dto.getMaKhoa()));

        entity.setKhoa(khoa);
        entity.setTen(dto.getTen());
        entity.setCapNhatLuc(Instant.now());

        Nganh saved = nganhRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_NGANH, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public void xoa(Integer ma, HttpServletRequest request) {
        if (nganhRepository.coChuyenNganhThuocNganh(ma)) {
            throw new BadRequestException("Không thể xóa ngành này vì còn chuyên ngành thuộc ngành.");
        }

        nganhRepository.deleteById(ma);
        nhatKyService.ghiNhatKy(HanhDong.XOA_NGANH, null, ma, request);
    }

    @Override
    @Transactional(readOnly = true)
    public NganhDTO timTheoMa(Integer ma) {
        Nganh entity = nganhRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Nganh", "ma", ma));
        return toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<NganhDTO> layDanhSachPhanTrang(
            String keyword, Integer maKhoa, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Nganh> page =
                nganhRepository.searchNganh(keyword, maKhoa, pageable);
        return page.map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NganhDTO> layTatCa() {
        return nganhRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NganhDTO> layTheoKhoa(Integer maKhoa) {
        return nganhRepository.findByKhoaMa(maKhoa).stream()
                .map(this::toDTO)
                .toList();
    }

    private NganhDTO toDTO(Nganh entity) {
        return NganhDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .maKhoa(entity.getKhoa() != null ? entity.getKhoa().getMa() : null)
                .build();
    }
}
