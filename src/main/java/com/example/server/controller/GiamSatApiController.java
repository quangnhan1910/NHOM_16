package com.example.server.controller;

import com.example.server.dto.GiamSatCaThiVm;
import com.example.server.dto.GiamSatTheoDoiBaiVm;
import com.example.server.dto.GiamSatTongHopVm;
import com.example.server.service.GiamSatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/giam-sat")
public class GiamSatApiController {

    private final GiamSatService giamSatService;

    public GiamSatApiController(GiamSatService giamSatService) {
        this.giamSatService = giamSatService;
    }

    /**
     * GET /api/giam-sat/tong-hop
     * Lấy danh sách tất cả ca thi đang diễn ra (admin).
     */
    @GetMapping("/tong-hop")
    public ResponseEntity<GiamSatTongHopVm> layTongHop() {
        return ResponseEntity.ok(giamSatService.layTongHopGiamSatTatCa());
    }

    /**
     * GET /api/giam-sat/tong-hop?maGiangVien=1
     * Lấy danh sách ca thi đang diễn ra của 1 giảng viên.
     */
    @GetMapping("/tong-hop-gv")
    public ResponseEntity<GiamSatTongHopVm> layTongHopGiangVien(
            @RequestParam Integer maGiangVien) {
        return ResponseEntity.ok(giamSatService.layTongHopGiamSat(maGiangVien));
    }

    /**
     * GET /api/giam-sat/chi-tiet?maCaThi=5
     * Lấy chi tiết 1 ca thi (sinh viên đang thi, đã nộp, ...).
     */
    @GetMapping("/chi-tiet")
    public ResponseEntity<GiamSatCaThiVm> layChiTiet(@RequestParam Integer maCaThi) {
        GiamSatCaThiVm chiTiet = giamSatService.layChiTietGiamSat(maCaThi);
        if (chiTiet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chiTiet);
    }

    /**
     * GET /api/giam-sat/theo-doi-bai-thi?maBaiThi=1&maCaThi=2
     * Chi tiết bài làm để giảng viên theo dõi (maCaThi tùy chọn, dùng để kiểm tra khớp).
     */
    @GetMapping("/theo-doi-bai-thi")
    public ResponseEntity<GiamSatTheoDoiBaiVm> layTheoDoiBaiThi(
            @RequestParam Integer maBaiThi,
            @RequestParam(required = false) Integer maCaThi) {
        return ResponseEntity.ok(giamSatService.layTheoDoiBaiThi(maBaiThi, maCaThi));
    }
}
