package com.example.server.service;

import com.example.server.dto.KhoaDTO;
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
import java.util.List;

@Service
@Transactional
public class KhoaServiceImpl implements KhoaService {

    private final KhoaRepository khoaRepository;
    private final TruongRepository truongRepository;
    private final NhatKyService nhatKyService;

    public KhoaServiceImpl(KhoaRepository khoaRepository,
                           TruongRepository truongRepository,
                           NhatKyService nhatKyService) {
        this.khoaRepository = khoaRepository;
        this.truongRepository = truongRepository;
        this.nhatKyService = nhatKyService;
    }

    @Override
    public KhoaDTO tao(KhoaDTO dto, HttpServletRequest request) {
        Truong truong = truongRepository.findById(dto.getMaTruong())
                .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", dto.getMaTruong()));

        Khoa entity = Khoa.builder()
                .truong(truong)
                .ten(dto.getTen())
                .taoLuc(Instant.now())
                .build();

        Khoa saved = khoaRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.THEM_KHOA, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public KhoaDTO capNhat(Integer ma, KhoaDTO dto, HttpServletRequest request) {
        Khoa entity = khoaRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", ma));

        Truong truong = truongRepository.findById(dto.getMaTruong())
                .orElseThrow(() -> new ResourceNotFoundException("Truong", "ma", dto.getMaTruong()));

        entity.setTruong(truong);
        entity.setTen(dto.getTen());
        entity.setCapNhatLuc(Instant.now());

        Khoa saved = khoaRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_KHOA, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public void xoa(Integer ma, HttpServletRequest request) {
        if (khoaRepository.coNganhThuocKhoa(ma)) {
            throw new BadRequestException("Không thể xóa khoa này vì còn ngành thuộc khoa.");
        }

        khoaRepository.deleteById(ma);
        nhatKyService.ghiNhatKy(HanhDong.XOA_KHOA, null, ma, request);
    }

    @Override
    @Transactional(readOnly = true)
    public KhoaDTO timTheoMa(Integer ma) {
        Khoa entity = khoaRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", ma));
        return toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KhoaDTO> layDanhSachPhanTrang(String keyword, Pageable pageable) {
        Page<Khoa> page = khoaRepository.searchKhoa(keyword, pageable);
        return page.map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhoaDTO> layTatCa() {
        return khoaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhoaDTO> layTheoTruong(Integer maTruong) {
        return khoaRepository.findByTruongMa(maTruong).stream()
                .map(this::toDTO)
                .toList();
    }

    private KhoaDTO toDTO(Khoa entity) {
        return KhoaDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .maTruong(entity.getTruong() != null
                        ? entity.getTruong().getMa()
                        : null)
                .build();
    }
}
