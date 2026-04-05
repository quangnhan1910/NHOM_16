package com.example.server.model.enums;

/**
 * Cấp bậc của trường: Đại học, Cao đẳng, Trung cấp.
 */
public enum CapBacTruong {
    DAI_HOC("Đại học"),
    CAO_DANG("Cao đẳng"),
    TRUNG_CAP("Trung cấp");

    private final String moTa;

    CapBacTruong(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
