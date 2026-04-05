package com.example.server.service;

import com.example.server.dto.MonHocDTO;
import com.example.server.exception.BadRequestException;
import com.example.server.exception.ResourceNotFoundException;
import com.example.server.model.Khoa;
import com.example.server.model.MonHoc;
import com.example.server.repository.KhoaRepository;
import com.example.server.repository.MonHocRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MonHocServiceImpl implements MonHocService {

    private final MonHocRepository monHocRepository;
    private final KhoaRepository khoaRepository;
    private final NhatKyService nhatKyService;

    @Override
    public MonHocDTO tao(MonHocDTO dto, HttpServletRequest httpRequest) {
        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", dto.getMaKhoa()));

        String maDD = dto.getMaDinhDanh() != null ? dto.getMaDinhDanh().trim() : null;
        if (maDD != null && !maDD.isEmpty() && monHocRepository.existsByMaDinhDanh(maDD)) {
            throw new BadRequestException("Mã môn học '" + maDD + "' đã tồn tại.");
        }

        MonHoc entity = MonHoc.builder()
                .ten(dto.getTen() != null ? dto.getTen().trim() : null)
                .maDinhDanh(maDD)
                .soTinChi(dto.getSoTinChi())
                .khoa(khoa)
                .taoLuc(Instant.now())
                .build();

        MonHoc saved = monHocRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.THEM_MON_HOC, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    @Override
    public MonHocDTO capNhat(Integer ma, MonHocDTO dto, HttpServletRequest httpRequest) {
        MonHoc entity = monHocRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc", "ma", ma));

        Khoa khoa = khoaRepository.findById(dto.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa", "ma", dto.getMaKhoa()));

        String maDD = dto.getMaDinhDanh() != null ? dto.getMaDinhDanh().trim() : null;
        if (maDD != null && !maDD.isEmpty()) {
            boolean trung = monHocRepository.existsByMaDinhDanh(maDD)
                    && !maDD.equals(entity.getMaDinhDanh());
            if (trung) {
                throw new BadRequestException("Mã môn học '" + maDD + "' đã tồn tại.");
            }
        }

        entity.setTen(dto.getTen() != null ? dto.getTen().trim() : null);
        entity.setMaDinhDanh(maDD);
        entity.setSoTinChi(dto.getSoTinChi());
        entity.setKhoa(khoa);
        entity.setCapNhatLuc(Instant.now());

        MonHoc saved = monHocRepository.save(entity);
        nhatKyService.ghiNhatKy(HanhDong.SUA_MON_HOC, null, saved.getMa(), httpRequest);
        return toDTO(saved);
    }

    @Override
    public void xoa(Integer ma, HttpServletRequest httpRequest) {
        MonHoc entity = monHocRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc", "ma", ma));

        // Kiểm tra có ràng buộc không
        boolean coDeThi = !entity.getDeThis().isEmpty();
        boolean coCauHoi = !entity.getNganHangCauHois().isEmpty();
        boolean coGiangVienMonHoc = !entity.getGiangVienMonHocs().isEmpty();

        if (coDeThi || coCauHoi || coGiangVienMonHoc) {
            throw new BadRequestException(
                    "Không thể xóa môn học này vì có ràng buộc dữ liệu "
                    + "(đề thi, câu hỏi, hoặc giảng viên phụ trách). "
                    + "Vui lòng xóa các ràng buộc trước.");
        }

        monHocRepository.deleteById(ma);
        nhatKyService.ghiNhatKy(HanhDong.XOA_MON_HOC, null, ma, httpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public MonHocDTO timTheoMa(Integer ma) {
        MonHoc entity = monHocRepository.findById(ma)
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc", "ma", ma));
        return toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonHocDTO> layDanhSachPhanTrang(String keyword, Integer maKhoa, Integer soTinChi, Pageable pageable) {
        return monHocRepository.searchMonHoc(keyword, maKhoa, soTinChi, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonHocDTO> layTatCa() {
        return monHocRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private MonHocDTO toDTO(MonHoc entity) {
        return MonHocDTO.builder()
                .ma(entity.getMa())
                .ten(entity.getTen())
                .maDinhDanh(entity.getMaDinhDanh())
                .soTinChi(entity.getSoTinChi())
                .maKhoa(entity.getKhoa() != null ? entity.getKhoa().getMa() : null)
                .tenKhoa(entity.getKhoa() != null ? entity.getKhoa().getTen() : null)
                .taoLuc(entity.getTaoLuc())
                .capNhatLuc(entity.getCapNhatLuc())
                .build();
    }
}
