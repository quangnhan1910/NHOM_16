package com.example.server.service;

import com.example.server.dto.DeThiTheoLoaiBar;
import com.example.server.dto.HoatDongGanDayVm;
import com.example.server.model.enums.LoaiDeThi;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gom số liệu thật cho bảng điều khiển giảng viên (toàn hệ thống).
 * Sau này có thể lọc theo maGiangVien đăng nhập.
 */
@Service
public class BangDieuKhienGiangVienService {

    private final CaThiRepository caThiRepository;
    private final NganHangCauHoiRepository nganHangCauHoiRepository;
    private final BaiThiSinhVienRepository baiThiSinhVienRepository;
    private final DeThiRepository deThiRepository;
    private final NhatKyHeThongRepository nhatKyHeThongRepository;

    public BangDieuKhienGiangVienService(
            CaThiRepository caThiRepository,
            NganHangCauHoiRepository nganHangCauHoiRepository,
            BaiThiSinhVienRepository baiThiSinhVienRepository,
            DeThiRepository deThiRepository,
            NhatKyHeThongRepository nhatKyHeThongRepository) {
        this.caThiRepository = caThiRepository;
        this.nganHangCauHoiRepository = nganHangCauHoiRepository;
        this.baiThiSinhVienRepository = baiThiSinhVienRepository;
        this.deThiRepository = deThiRepository;
        this.nhatKyHeThongRepository = nhatKyHeThongRepository;
    }

    public long demTongCaThi() {
        return caThiRepository.count();
    }

    public long demTongCauHoi() {
        return nganHangCauHoiRepository.count();
    }

    public long demLuotLamBai() {
        return baiThiSinhVienRepository.count();
    }

    public long demCaTheoTrangThai(TrangThaiCaThi tt) {
        return caThiRepository.countByTrangThai(tt);
    }

    /** Tổng ca dùng tính % thanh tiến trình (bỏ DA_HUY khỏi mẫu số hoặc gộp). */
    public long tongCaChoThongKe() {
        return demCaTheoTrangThai(TrangThaiCaThi.DA_KET_THUC)
                + demCaTheoTrangThai(TrangThaiCaThi.DANG_DIEN_RA)
                + demCaTheoTrangThai(TrangThaiCaThi.CHO_DANG_KY);
    }

    public int phanTram(long so, long tong) {
        if (tong <= 0 || so <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(100.0 * so / tong));
    }

    public List<DeThiTheoLoaiBar> deThiTheoLoai() {
        long gk = deThiRepository.countByLoaiDeThi(LoaiDeThi.GIUA_KY);
        long ck = deThiRepository.countByLoaiDeThi(LoaiDeThi.CUOI_KY);
        long th = deThiRepository.countByLoaiDeThi(LoaiDeThi.THUC_HANH);
        long max = Math.max(Math.max(gk, ck), th);
        List<DeThiTheoLoaiBar> list = new ArrayList<>();
        list.add(new DeThiTheoLoaiBar("Giữa kỳ", gk, chieuCao(gk, max)));
        list.add(new DeThiTheoLoaiBar("Cuối kỳ", ck, chieuCao(ck, max)));
        list.add(new DeThiTheoLoaiBar("Thực hành", th, chieuCao(th, max)));
        return list;
    }

    private static int chieuCao(long so, long max) {
        if (so <= 0 || max <= 0) {
            return 0;
        }
        int p = (int) Math.round(100.0 * so / max);
        return Math.max(p, 12); /* cột nhỏ vẫn nhìn thấy */
    }

    private static final DateTimeFormatter DTF_LOG =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public List<HoatDongGanDayVm> hoatDongGanDay() {
        return nhatKyHeThongRepository.findTop10BriefByOrderByTaoLucDesc().stream()
                .map(this::toHoatDongVm)
                .toList();
    }

    private HoatDongGanDayVm toHoatDongVm(NhatKyHeThongBrief nk) {
        String tg = nk.getTaoLuc() != null ? DTF_LOG.format(nk.getTaoLuc()) : "—";
        String hd = nk.getHanhDong() != null ? nk.getHanhDong() : "—";
        return new HoatDongGanDayVm(hd, nk.getBangMucTieu(), tg);
    }
}
