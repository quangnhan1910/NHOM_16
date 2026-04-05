package com.example.server.dto;

import com.example.server.model.enums.TrangThaiCaThi;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO gắn kết dữ liệu form tạo mới / chỉnh sửa Ca Thi.
 * Dùng LocalDateTime để nhận dữ liệu từ input datetime-local (Asia/Ho_Chi_Minh).
 * Service sẽ chuyển đổi sang Instant khi lưu vào DB.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaThiFormDTO {

    @NotNull(message = "Vui lòng chọn đề thi")
    private Integer maDeThi;

    @NotNull(message = "Vui lòng chọn khoa tổ chức")
    private Integer maKhoa;

    @NotBlank(message = "Tên ca thi không được để trống")
    @Size(max = 255, message = "Tên ca thi không được vượt quá 255 ký tự")
    private String tenCaThi;

    @NotNull(message = "Vui lòng nhập thời gian bắt đầu")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime thoiGianBatDau;

    @NotNull(message = "Vui lòng nhập thời gian kết thúc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime thoiGianKetThuc;

    @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
    private String diaDiem;

    @Min(value = 1, message = "Số lượng tối đa phải ít nhất là 1")
    private Integer soLuongToiDa;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private TrangThaiCaThi trangThai;
}
