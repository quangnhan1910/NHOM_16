package com.example.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller serve các trang HTML cho đề thi.
 * Dùng chung cho QUAN_TRI_VIEN và GIANG_VIEN.
 */
@Controller
@RequestMapping("/de-thi")
public class DeThiController {

    @GetMapping({"", "/", "/quan-ly"})
    public String viewQuanLyDeThi() {
        return "de-thi/quan-ly-de-thi";
    }

    @GetMapping("/them-moi")
    public String viewThemDeThi() {
        return "de-thi/them-de-thi";
    }

    @GetMapping("/sua")
    public String viewSuaDeThi() {
        return "de-thi/sua-de-thi";
    }
}
