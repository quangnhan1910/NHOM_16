package com.example.server.service;

import com.example.server.dto.TaoPhanCongGiangDayRequest;
import com.example.server.model.*;
import com.example.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhanCongGiangDayService {

    private final GiangVienMonHocRepository giangVienMonHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;
    private final ChuyenNganhRepository chuyenNganhRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienHoSoTuDongService giangVienHoSoTuDongService;

    public PhanCongGiangDayService(
            GiangVienMonHocRepository giangVienMonHocRepository,
            GiangVienRepository giangVienRepository,
            MonHocRepository monHocRepository,
            ChuyenNganhRepository chuyenNganhRepository,
            SinhVienRepository sinhVienRepository,
            GiangVienHoSoTuDongService giangVienHoSoTuDongService) {
        this.giangVienMonHocRepository = giangVienMonHocRepository;
        this.giangVienRepository = giangVienRepository;
        this.monHocRepository = monHocRepository;
        this.chuyenNganhRepository = chuyenNganhRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienHoSoTuDongService = giangVienHoSoTuDongService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> layTatCaPhanCong() {
        return giangVienMonHocRepository.findAllChiTietPhanCong().stream()
                .map(this::chuyenDoiHang)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Map<String, Object>> layGiangVienChoChon() {
        giangVienHoSoTuDongService.damBaoHoSoChoTatCaTaiKhoanGiangVien();
        return giangVienRepository.findAllCoNguoiDung().stream()
                .map(gv -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ma", gv.getMa());
                    m.put("maNhanVien", gv.getMaNhanVien() != null ? gv.getMaNhanVien() : "");
                    if (gv.getNguoiDung() != null) {
                        m.put("hoTen", gv.getNguoiDung().getHoTen() != null ? gv.getNguoiDung().getHoTen() : "");
                    } else {
                        m.put("hoTen", "");
                    }
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> laySinhVienChoChon() {
        return sinhVienRepository.findAllCoNguoiDung().stream()
                .map(sv -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ma", sv.getMa());
                    m.put("mssv", sv.getMaSinhVien() != null ? sv.getMaSinhVien() : "");
                    if (sv.getNguoiDung() != null) {
                        m.put("hoTen", sv.getNguoiDung().getHoTen() != null ? sv.getNguoiDung().getHoTen() : "");
                    } else {
                        m.put("hoTen", "");
                    }
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> taoPhanCong(TaoPhanCongGiangDayRequest req) {
        GiangVien gv = giangVienRepository.findById(req.getMaGiangVien())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên."));
        MonHoc mh = monHocRepository.findById(req.getMaMonHoc())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học."));

        ChuyenNganh cn = null;
        if (req.getMaChuyenNganh() != null) {
            cn = chuyenNganhRepository.findById(req.getMaChuyenNganh())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên ngành (lớp)."));
        }
        List<Integer> maSinhViens = chuanHoaMaSinhViens(req);

        if (giangVienMonHocRepository.existsTrungPhanCong(
                gv.getMa(), mh.getMa(), cn != null ? cn.getMa() : null)) {
            throw new IllegalArgumentException(
                    "Phân công này đã tồn tại (trùng giảng viên, môn và nhóm lớp). Chỉnh sửa bản ghi hiện có để thêm sinh viên.");
        }

        GiangVienMonHoc entity = GiangVienMonHoc.builder()
                .giangVien(gv)
                .monHoc(mh)
                .chuyenNganh(cn)
                .build();
        apDungLichThuCong(entity, req);
        dongBoSinhVienPhanCong(entity, maSinhViens);
        GiangVienMonHoc saved = giangVienMonHocRepository.save(entity);
        return chuyenDoiHang(saved);
    }

    @Transactional
    public Map<String, Object> capNhatPhanCong(Integer ma, TaoPhanCongGiangDayRequest req) {
        GiangVienMonHoc entity = giangVienMonHocRepository.findById(ma)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi phân công."));
        GiangVien gv = giangVienRepository.findById(req.getMaGiangVien())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên."));
        MonHoc mh = monHocRepository.findById(req.getMaMonHoc())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học."));
        ChuyenNganh cn = null;
        if (req.getMaChuyenNganh() != null) {
            cn = chuyenNganhRepository.findById(req.getMaChuyenNganh())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên ngành (lớp)."));
        }
        List<Integer> maSinhViens = chuanHoaMaSinhViens(req);
        if (giangVienMonHocRepository.existsTrungPhanCongBoQuaMa(
                gv.getMa(), mh.getMa(), cn != null ? cn.getMa() : null, ma)) {
            throw new IllegalArgumentException(
                    "Phân công này đã tồn tại (trùng giảng viên, môn và nhóm lớp).");
        }
        entity.setGiangVien(gv);
        entity.setMonHoc(mh);
        entity.setChuyenNganh(cn);
        apDungLichThuCong(entity, req);
        dongBoSinhVienPhanCong(entity, maSinhViens);
        GiangVienMonHoc saved = giangVienMonHocRepository.save(entity);
        return chuyenDoiHang(saved);
    }

    @Transactional
    public void xoaPhanCong(Integer ma) {
        if (!giangVienMonHocRepository.existsById(ma)) {
            throw new IllegalArgumentException("Không tìm thấy bản ghi phân công.");
        }
        giangVienMonHocRepository.deleteById(ma);
    }

    private Map<String, Object> chuyenDoiHang(GiangVienMonHoc g) {
        Map<String, Object> m = new HashMap<>();
        m.put("ma", g.getMa());
        m.put("maGiangVien", g.getGiangVien() != null ? g.getGiangVien().getMa() : null);
        if (g.getGiangVien() != null && g.getGiangVien().getNguoiDung() != null) {
            m.put("tenGiangVien", g.getGiangVien().getNguoiDung().getHoTen());
        } else {
            m.put("tenGiangVien", "");
        }
        m.put("maMonHoc", g.getMonHoc() != null ? g.getMonHoc().getMa() : null);
        m.put("tenMonHoc", g.getMonHoc() != null && g.getMonHoc().getTen() != null ? g.getMonHoc().getTen() : "");
        m.put("maDinhDanhMon", g.getMonHoc() != null && g.getMonHoc().getMaDinhDanh() != null
                ? g.getMonHoc().getMaDinhDanh() : "");
        if (g.getChuyenNganh() != null) {
            m.put("maChuyenNganh", g.getChuyenNganh().getMa());
            m.put("tenChuyenNganh", g.getChuyenNganh().getTen() != null ? g.getChuyenNganh().getTen() : "");
        } else {
            m.put("maChuyenNganh", null);
            m.put("tenChuyenNganh", "");
        }
        List<Map<String, Object>> svs = new ArrayList<>();
        if (g.getSinhVienPhanCongs() != null) {
            for (GiangVienMonHocSinhVien l : g.getSinhVienPhanCongs()) {
                SinhVien s = l.getSinhVien();
                if (s == null) {
                    continue;
                }
                Map<String, Object> sm = new HashMap<>();
                sm.put("ma", s.getMa());
                sm.put("mssv", s.getMaSinhVien() != null ? s.getMaSinhVien() : "");
                if (s.getNguoiDung() != null) {
                    sm.put("tenSinhVien", s.getNguoiDung().getHoTen() != null
                            ? s.getNguoiDung().getHoTen() : "");
                } else {
                    sm.put("tenSinhVien", "");
                }
                svs.add(sm);
            }
        }
        m.put("sinhViens", svs);
        if (!svs.isEmpty()) {
            Map<String, Object> first = svs.get(0);
            m.put("maSinhVien", first.get("ma"));
            m.put("mssv", first.get("mssv"));
            m.put("tenSinhVien", first.get("tenSinhVien"));
        } else {
            m.put("maSinhVien", null);
            m.put("mssv", "");
            m.put("tenSinhVien", "");
        }
        m.put("lichBuoi", g.getLichBuoi() != null ? g.getLichBuoi() : "");
        m.put("lichNgay", g.getLichNgay() != null ? g.getLichNgay().toString() : "");
        m.put("lichTietBatDau", g.getLichTietBatDau() != null ? String.valueOf(g.getLichTietBatDau()) : "");
        m.put("lichTietKetThuc", g.getLichTietKetThuc() != null ? String.valueOf(g.getLichTietKetThuc()) : "");
        m.put("lichPhong", g.getLichPhong() != null ? g.getLichPhong() : "");
        m.put("lichNoiDung", g.getLichNoiDung() != null ? g.getLichNoiDung() : "");
        m.put("lichTrangThai", g.getLichTrangThai() != null ? g.getLichTrangThai() : "");
        return m;
    }

    private static List<Integer> chuanHoaMaSinhViens(TaoPhanCongGiangDayRequest req) {
        if (req.getMaSinhViens() != null && !req.getMaSinhViens().isEmpty()) {
            return req.getMaSinhViens().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (req.getMaSinhVien() != null) {
            return List.of(req.getMaSinhVien());
        }
        return List.of();
    }

    private void dongBoSinhVienPhanCong(GiangVienMonHoc gvmh, List<Integer> maSinhViens) {
        if (gvmh.getSinhVienPhanCongs() == null) {
            gvmh.setSinhVienPhanCongs(new ArrayList<>());
        }
        gvmh.getSinhVienPhanCongs().clear();
        // Bắt buộc xóa bản ghi cũ trên DB trước khi INSERT lại — nếu không MySQL báo trùng uk_gvmh_ma_sinh_vien
        if (gvmh.getMa() != null) {
            giangVienMonHocRepository.flush();
        }
        Set<Integer> daThem = new LinkedHashSet<>();
        for (Integer maSv : maSinhViens) {
            if (maSv == null || !daThem.add(maSv)) {
                continue;
            }
            SinhVien sv = sinhVienRepository.findById(maSv)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
            gvmh.getSinhVienPhanCongs().add(GiangVienMonHocSinhVien.builder()
                    .giangVienMonHoc(gvmh)
                    .sinhVien(sv)
                    .build());
        }
    }

    private void apDungLichThuCong(GiangVienMonHoc e, TaoPhanCongGiangDayRequest r) {
        e.setLichBuoi(chuoiRongThanhNull(r.getLichBuoi()));
        e.setLichNgay(parseLocalDateOrNull(r.getLichNgay()));
        e.setLichTietBatDau(parseIntegerOrNull(r.getLichTietBatDau()));
        e.setLichTietKetThuc(parseIntegerOrNull(r.getLichTietKetThuc()));
        e.setLichPhong(chuoiRongThanhNull(r.getLichPhong()));
        e.setLichNoiDung(chuoiRongThanhNull(r.getLichNoiDung()));
        e.setLichTrangThai(chuoiRongThanhNull(r.getLichTrangThai()));
    }

    private static String chuoiRongThanhNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseLocalDateOrNull(String s) {
        String t = chuoiRongThanhNull(s);
        if (t == null) {
            return null;
        }
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static Integer parseIntegerOrNull(String s) {
        String t = chuoiRongThanhNull(s);
        if (t == null) {
            return null;
        }
        try {
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
