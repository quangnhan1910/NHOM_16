package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/lecturer")
public class ThongKeController {

    @GetMapping({"/thong-ke-ket-qua-thi", "/thong-ke"})
    public String trangThongKe(Model model) {
        return "lecturer/thong-ke-ket-qua-thi";
    }

    @GetMapping("/thong-ke/chi-tiet")
    public String chiTietThongKe(@RequestParam Integer maCaThi, Model model) {
        model.addAttribute("maCaThi", maCaThi);
        return "lecturer/thong-ke-ket-qua-thi";
    }
}
