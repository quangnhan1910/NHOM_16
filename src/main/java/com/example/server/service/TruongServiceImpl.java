package com.example.server.service;

import com.example.server.dto.TruongDTO;
import com.example.server.exception.BadRequestException;
import com.example.server.exception.ResourceNotFoundException;
import com.example.server.model.Khoa;
import com.example.server.model.Truong;
import com.example.server.repository.KhoaRepository;
import com.example.server.repository.TruongRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TruongServiceImpl implements TruongService {

    private final TruongRepository truongRepository;
    private final KhoaRepository khoaRepository;
    private final NhatKyService nhatKyService;

    public TruongServiceImpl(TruongRepository truongRepository,
                             KhoaRepository khoaRepository,
                             NhatKyService nhatKyService) {
        this.truongRepository = truongRepository;
        this.khoaRepository = khoaRepository;
        this.nhatKyService = nhatKyService;
    }

    @Override
    public TruongDTO tao(TruongDTO dto, HttpServletRequest request) {
        if (dto.getMaDinhDanh() != null
                && truongRepository.existsByMaDinhDanh(dto.getMaDinhDanh())) {
            throw new BadRequestException("Mã định danh '" + dto.getMaDinhDanh() + "' đã tồn tại.");
        }

        Truong entity = Truong.builder()
                .ten(dto.getTen())
                .capBac(dto.getCapBac())
                .maDinhDanh(dto.getMaDinhDanh())
                .diaChi(dto.getDiaChi())
                .taoLuc(Instant.now())
                .build();

        Truong saved = truongRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.THEM_TRUONG, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public List<TruongDTO> taoKemNhieuKhoa(TruongDTO truongDTO, List<String> tenKhoas, HttpServletRequest request) {
        if (truongDTO.getMaDinhDanh() != null
                && truongRepository.existsByMaDinhDanh(truongDTO.getMaDinhDanh())) {
            throw new BadRequestException("Mã định danh '" + truongDTO.getMaDinhDanh() + "' đã tồn tại.");
        }

        Truong truong = Truong.builder()
                .ten(truongDTO.getTen())
                .capBac(truongDTO.getCapBac())
                .maDinhDanh(truongDTO.getMaDinhDanh())
                .diaChi(truongDTO.getDiaChi())
                .taoLuc(Instant.now())
                .build();

        Truong truongSaved = truongRepository.save(truong);

        nhatKyService.ghiNhatKy(HanhDong.THEM_TRUONG, null, truongSaved.getMa(), request);

        List<TruongDTO> result = new ArrayList<>();
        result.add(toDTO(truongSaved));

        if (tenKhoas != null) {
            for (String tenKhoa : tenKhoas) {
                if (tenKhoa != null && !tenKhoa.trim().isEmpty()) {
                    Khoa khoa = Khoa.builder()
                            .truong(truongSaved)
                            .ten(tenKhoa.trim())
                            .taoLuc(Instant.now())
                            .build();
                    khoaRepository.save(khoa);
                    nhatKyService.ghiNhatKy(HanhDong.THEM_KHOA, null, khoa.getMa(), request);
                    result.add(toDTO(khoa));
                }
            }
        }
        return result;
    }

    @Override
    public TruongDTO capNhat(Integer ma, TruongDTO dto, HttpServletRequest request) {
        Truong entity = truongRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", ma));

        if (dto.getMaDinhDanh() != null) {
            boolean trung = truongRepository.existsByMaDinhDanh(dto.getMaDinhDanh())
                    && !dto.getMaDinhDanh().equals(entity.getMaDinhDanh());
            if (trung) {
                throw new BadRequestException("Mã định danh '" + dto.getMaDinhDanh() + "' đã tồn tại.");
            }
        }

        entity.setTen(dto.getTen());
        entity.setCapBac(dto.getCapBac());
        entity.setMaDinhDanh(dto.getMaDinhDanh());
        entity.setDiaChi(dto.getDiaChi());
        entity.setCapNhatLuc(Instant.now());

        Truong saved = truongRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_TRUONG, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public void xoa(Integer ma, HttpServletRequest request) {
        if (truongRepository.existsById(ma)
                && khoaRepository.existsByTruongMa(ma)) {
            throw new BadRequestException("Không thể xóa trường này vì còn khoa thuộc trường.");
        }

        truongRepository.deleteById(ma);
        nhatKyService.ghiNhatKy(HanhDong.XOA_TRUONG, null, ma, request);
    }

    @Override
    @Transactional(readOnly = true)
    public TruongDTO timTheoMa(Integer ma) {
        Truong entity = truongRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", ma));
        return toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TruongDTO> layDanhSachPhanTrang(String keyword, Pageable pageable) {
        Page<Truong> page = truongRepository.searchTruong(keyword, pageable);
        return page.map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TruongDTO> layTatCa() {
        return truongRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private TruongDTO toDTO(Truong entity) {
        return TruongDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .capBac(entity.getCapBac())
                .maDinhDanh(entity.getMaDinhDanh())
                .diaChi(entity.getDiaChi())
                .build();
    }

    private TruongDTO toDTO(Khoa entity) {
        return TruongDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .build();
    }
}
