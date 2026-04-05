package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/lecturer")
public class GiamSatController {

    /** Trang danh sách ca thi đang diễn ra — alias ngắn: /lecturer/giam-sat */
    @GetMapping({"/giam-sat", "/giam-sat-thi"})
    public String trangGiamSat(Model model) {
        return "lecturer/giam-sat";
    }

    @GetMapping({"/giam-sat/chi-tiet", "/giam-sat-thi/chi-tiet"})
    public String chiTietGiamSat(@RequestParam Integer maCaThi, Model model) {
        model.addAttribute("maCaThi", maCaThi);
        return "lecturer/giam-sat-chi-tiet";
    }

    @GetMapping({"/giam-sat-thi/theo-doi-bai", "/giam-sat/theo-doi-bai"})
    public String theoDoiBaiThi(
            @RequestParam Integer maCaThi,
            @RequestParam Integer maBaiThi,
            Model model) {
        model.addAttribute("maCaThi", maCaThi);
        model.addAttribute("maBaiThi", maBaiThi);
        return "lecturer/giam-sat-theo-doi-bai";
    }
}
