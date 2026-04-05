package com.example.server.service;

import com.example.server.dto.DanhSachSinhVienVm;
import com.example.server.dto.SinhVienTrongLopVm;
import com.example.server.model.GiangVienMonHoc;
import com.example.server.model.GiangVienMonHocSinhVien;
import com.example.server.model.MonHoc;
import com.example.server.model.SinhVien;
import com.example.server.repository.DangKyThiRepository;
import com.example.server.repository.GiangVienMonHocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DanhSachSinhVienService {

    private final GiangVienMonHocRepository giangVienMonHocRepository;
    private final DangKyThiRepository dangKyThiRepository;

    public DanhSachSinhVienService(
            GiangVienMonHocRepository giangVienMonHocRepository,
            DangKyThiRepository dangKyThiRepository) {
        this.giangVienMonHocRepository = giangVienMonHocRepository;
        this.dangKyThiRepository = dangKyThiRepository;
    }

    /**
     * Lấy danh sách sinh viên: đăng ký thi theo môn + sinh viên gắn phân công (nếu có và chưa trùng).
     */
    @Transactional(readOnly = true)
    public DanhSachSinhVienVm layDanhSachSinhVien(Integer maGiangVienMonHoc) {
        GiangVienMonHoc gvmh = giangVienMonHocRepository
                .findByMaWithMonHocVaSinhVienPhanCong(maGiangVienMonHoc)
                .orElse(null);

        if (gvmh == null || gvmh.getMonHoc() == null) {
            return null;
        }

        MonHoc monHoc = gvmh.getMonHoc();
        Integer maMonHoc = monHoc.getMa();

        List<SinhVien> tuDangKy = dangKyThiRepository.findSinhVienByMonHocMa(maMonHoc);
        Set<Integer> daCo = new LinkedHashSet<>();
        List<SinhVien> danhSachSv = new ArrayList<>();
        if (gvmh.getSinhVienPhanCongs() != null) {
            for (GiangVienMonHocSinhVien l : gvmh.getSinhVienPhanCongs()) {
                SinhVien sv = l.getSinhVien();
                if (sv != null && daCo.add(sv.getMa())) {
                    danhSachSv.add(sv);
                }
            }
        }
        for (SinhVien sv : tuDangKy) {
            if (daCo.add(sv.getMa())) {
                danhSachSv.add(sv);
            }
        }

        List<SinhVienTrongLopVm> danhSach = danhSachSv.stream()
                .map(this::chuyenDoiSangVm)
                .collect(Collectors.toList());

        return DanhSachSinhVienVm.builder()
                .maMonHoc(maMonHoc)
                .tenMonHoc(monHoc.getTen() != null ? monHoc.getTen() : "Chưa đặt tên")
                .tongSoSinhVien(danhSach.size())
                .danhSach(danhSach)
                .build();
    }

    private SinhVienTrongLopVm chuyenDoiSangVm(SinhVien sv) {
        String hoVaTen = "Chưa có tên";
        String email = "";
        String lopSh = "Chưa phân lớp";

        if (sv.getNguoiDung() != null) {
            hoVaTen = sv.getNguoiDung().getHoTen() != null 
                ? sv.getNguoiDung().getHoTen() 
                : "Chưa có tên";
            email = sv.getNguoiDung().getThuDienTu() != null 
                ? sv.getNguoiDung().getThuDienTu() 
                : "";
        }

        if (sv.getChuyenNganh() != null && sv.getChuyenNganh().getNganh() != null) {
            var nganh = sv.getChuyenNganh().getNganh();
            if (nganh.getTen() != null) {
                lopSh = nganh.getTen();
            }
        }

        return SinhVienTrongLopVm.builder()
                .maSinhVien(sv.getMa())
                .maSinhVienStr(sv.getMaSinhVien() != null ? sv.getMaSinhVien() : "")
                .hoVaTen(hoVaTen)
                .lopSinhHoat(lopSh)
                .email(email)
                .tinhTrang("DU_DIEU_KIEN") // Mặc định đủ điều kiện
                .build();
    }
}
