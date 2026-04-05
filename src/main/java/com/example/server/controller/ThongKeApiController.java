package com.example.server.controller;

import com.example.server.dto.KetQuaThiSinhVienVm;
import com.example.server.dto.ThongKeCaThiVm;
import com.example.server.dto.ThongKeTongHopVm;
import com.example.server.service.KhoaHocCuaToiService;
import com.example.server.service.ThongKeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeApiController {

    private final ThongKeService thongKeService;
    private final KhoaHocCuaToiService khoaHocCuaToiService;

    public ThongKeApiController(ThongKeService thongKeService,
                                KhoaHocCuaToiService khoaHocCuaToiService) {
        this.thongKeService = thongKeService;
        this.khoaHocCuaToiService = khoaHocCuaToiService;
    }

    /**
     * GET /api/thong-ke/tong-hop — theo giảng viên đăng nhập.
     * GET /api/thong-ke/tong-hop?maGiangVien=1 — chỉ định mã (vd. quản trị).
     */
    @GetMapping("/tong-hop")
    public ResponseEntity<ThongKeTongHopVm> layTongHop(@RequestParam(required = false) Integer maGiangVien) {
        Integer ma = maGiangVien != null
                ? maGiangVien
                : khoaHocCuaToiService.layMaGiangVienDangNhap().orElse(null);
        if (ma == null) {
            return ResponseEntity.ok(thongKeService.layTongHopThongKeRong());
        }
        return ResponseEntity.ok(thongKeService.layTongHopThongKe(ma));
    }

    @GetMapping("/chi-tiet-ca-thi")
    public ResponseEntity<ThongKeCaThiVm> layThongKeCaThi(@RequestParam Integer maCaThi) {
        ThongKeCaThiVm result = thongKeService.layThongKeChiTiet(maCaThi);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ket-qua-sinh-vien")
    public ResponseEntity<List<KetQuaThiSinhVienVm>> layKetQuaSinhVien(@RequestParam Integer maCaThi) {
        return ResponseEntity.ok(thongKeService.layKetQuaThiSinhVien(maCaThi));
    }
}
