package com.example.server.service;

import com.example.server.dto.ChuyenNganhDTO;
import com.example.server.exception.ResourceNotFoundException;
import com.example.server.model.ChuyenNganh;
import com.example.server.model.Nganh;
import com.example.server.repository.ChuyenNganhRepository;
import com.example.server.repository.NganhRepository;
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
public class ChuyenNganhServiceImpl implements ChuyenNganhService {

    private final ChuyenNganhRepository chuyenNganhRepository;
    private final NganhRepository nganhRepository;
    private final NhatKyService nhatKyService;

    public ChuyenNganhServiceImpl(ChuyenNganhRepository chuyenNganhRepository,
                                  NganhRepository nganhRepository,
                                  NhatKyService nhatKyService) {
        this.chuyenNganhRepository = chuyenNganhRepository;
        this.nganhRepository = nganhRepository;
        this.nhatKyService = nhatKyService;
    }

    @Override
    public ChuyenNganhDTO tao(ChuyenNganhDTO dto, HttpServletRequest request) {
        Nganh nganh = nganhRepository.findById(dto.getMaNganh())
                .orElseThrow(() -> new ResourceNotFoundException("Nganh", "ma", dto.getMaNganh()));

        ChuyenNganh entity = ChuyenNganh.builder()
                .nganh(nganh)
                .ten(dto.getTen())
                .taoLuc(Instant.now())
                .build();

        ChuyenNganh saved = chuyenNganhRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.THEM_CHUYEN_NGANH, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public List<ChuyenNganhDTO> taoKemNhieuChuyenNganh(Integer maNganh, List<String> tenChuyenNganhs, HttpServletRequest request) {
        Nganh nganh = nganhRepository.findById(maNganh)
                .orElseThrow(() -> new ResourceNotFoundException("Nganh", "ma", maNganh));

        List<ChuyenNganhDTO> result = new ArrayList<>();
        if (tenChuyenNganhs != null) {
            for (String tenCN : tenChuyenNganhs) {
                if (tenCN != null && !tenCN.trim().isEmpty()) {
                    ChuyenNganh cn = ChuyenNganh.builder()
                            .nganh(nganh)
                            .ten(tenCN.trim())
                            .taoLuc(Instant.now())
                            .build();
                    cn = chuyenNganhRepository.save(cn);
                    nhatKyService.ghiNhatKy(HanhDong.THEM_CHUYEN_NGANH, null, cn.getMa(), request);
                    result.add(toDTO(cn));
                }
            }
        }
        return result;
    }

    @Override
    public ChuyenNganhDTO capNhat(Integer ma, ChuyenNganhDTO dto, HttpServletRequest request) {
        ChuyenNganh entity = chuyenNganhRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("ChuyenNganh", "ma", ma));

        Nganh nganh = nganhRepository.findById(dto.getMaNganh())
                .orElseThrow(() -> new ResourceNotFoundException("Nganh", "ma", dto.getMaNganh()));

        entity.setNganh(nganh);
        entity.setTen(dto.getTen());
        entity.setCapNhatLuc(Instant.now());

        ChuyenNganh saved = chuyenNganhRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_CHUYEN_NGANH, null, saved.getMa(), request);
        return toDTO(saved);
    }

    @Override
    public void xoa(Integer ma, HttpServletRequest request) {
        chuyenNganhRepository.deleteById(ma);
        nhatKyService.ghiNhatKy(HanhDong.XOA_CHUYEN_NGANH, null, ma, request);
    }

    @Override
    @Transactional(readOnly = true)
    public ChuyenNganhDTO timTheoMa(Integer ma) {
        ChuyenNganh entity = chuyenNganhRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("ChuyenNganh", "ma", ma));
        return toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChuyenNganhDTO> layDanhSachPhanTrang(
            String keyword, Integer maNganh, Pageable pageable) {
        Page<ChuyenNganh> page =
                chuyenNganhRepository.searchChuyenNganh(keyword, maNganh, pageable);
        return page.map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChuyenNganhDTO> layTatCa() {
        return chuyenNganhRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChuyenNganhDTO> layTheoNganh(Integer maNganh) {
        return chuyenNganhRepository.findByNganhMa(maNganh).stream()
                .map(this::toDTO)
                .toList();
    }

    private ChuyenNganhDTO toDTO(ChuyenNganh entity) {
        return ChuyenNganhDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .maNganh(entity.getNganh() != null ? entity.getNganh().getMa() : null)
                .build();
    }
}
