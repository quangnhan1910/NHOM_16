package com.example.server.controller;

import com.example.server.model.*;
import com.example.server.model.enums.TrangThaiBaiThi;
import com.example.server.model.enums.TrangThaiCaThi;
import com.example.server.repository.*;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StudentClientApiController {
    private final SinhVienRepository sinhVienRepository;
    private final DangKyThiRepository dangKyThiRepository;
    private final CaThiRepository caThiRepository;
    private final CauHoiDeThiRepository cauHoiDeThiRepository;
    private final BaiThiSinhVienRepository baiThiSinhVienRepository;
    private final CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository;
    private final LuaChonCauTraLoiRepository luaChonCauTraLoiRepository;
    private final LuaChonCauHoiRepository luaChonCauHoiRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;

    public StudentClientApiController(
            SinhVienRepository sinhVienRepository,
            DangKyThiRepository dangKyThiRepository,
            CaThiRepository caThiRepository,
            CauHoiDeThiRepository cauHoiDeThiRepository,
            BaiThiSinhVienRepository baiThiSinhVienRepository,
            CauTraLoiSinhVienRepository cauTraLoiSinhVienRepository,
            LuaChonCauTraLoiRepository luaChonCauTraLoiRepository,
            LuaChonCauHoiRepository luaChonCauHoiRepository,
            PasswordEncoder passwordEncoder,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.sinhVienRepository = sinhVienRepository;
        this.dangKyThiRepository = dangKyThiRepository;
        this.caThiRepository = caThiRepository;
        this.cauHoiDeThiRepository = cauHoiDeThiRepository;
        this.baiThiSinhVienRepository = baiThiSinhVienRepository;
        this.cauTraLoiSinhVienRepository = cauTraLoiSinhVienRepository;
        this.luaChonCauTraLoiRepository = luaChonCauTraLoiRepository;
        this.luaChonCauHoiRepository = luaChonCauHoiRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/auth/login")
    @Transactional(readOnly = true)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String mssv = request.getMssv() == null ? "" : request.getMssv().trim();
        String password = request.getPassword() == null ? "" : request.getPassword().trim();
        if (mssv.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu MSSV hoặc mật khẩu");
        }

        SinhVien sv = sinhVienRepository.findByMaSinhVienWithNguoiDung(mssv)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không hợp lệ"));

        if (sv.getNguoiDung() == null || Boolean.FALSE.equals(sv.getNguoiDung().getTrangThaiHoatDong())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không hoạt động");
        }

        boolean ok = passwordEncoder.matches(password, sv.getNguoiDung().getMatKhauMaHoa());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không hợp lệ");
        }

        LoginResponse response = new LoginResponse();
        response.setStudentId(sv.getMa() == null ? null : sv.getMa().longValue());
        response.setMssv(sv.getMaSinhVien());
        response.setFullName(sv.getNguoiDung().getHoTen());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    @Transactional(readOnly = true)
    public List<SessionDto> sessions(@RequestParam("studentId") Long studentId) {
        SinhVien sv = requireSinhVien(studentId);
        List<DangKyThi> dangKyThis = dangKyThiRepository.findBySinhVienMaWithDetails(sv.getMa());
        List<SessionDto> result = new ArrayList<>();
        for (DangKyThi dk : dangKyThis) {
            if (dk.getCaThi() == null || dk.getCaThi().getMa() == null) {
                continue;
            }
            if (daHoanThanhCaThi(dk.getCaThi().getMa(), sv.getMa())) {
                continue;
            }
            String tenCaThi = dk.getCaThi().getTenCaThi() == null ? "" : dk.getCaThi().getTenCaThi().trim();
            if (tenCaThi.isEmpty()) {
                continue;
            }
            SessionDto dto = new SessionDto();
            dto.setId(dk.getCaThi().getMa().longValue());
            dto.setName(tenCaThi);
            String monHoc = (dk.getCaThi().getDeThi() != null && dk.getCaThi().getDeThi().getMonHoc() != null)
                    ? dk.getCaThi().getDeThi().getMonHoc().getTen()
                    : null;
            dto.setSubject(monHoc);
            Integer durationSeconds = null;
            if (dk.getCaThi().getDeThi() != null && dk.getCaThi().getDeThi().getThoiLuongPhut() != null) {
                durationSeconds = dk.getCaThi().getDeThi().getThoiLuongPhut() * 60;
            }
            dto.setDurationSeconds(durationSeconds);
            dto.setLocation(dk.getCaThi().getDiaDiem());
            dto.setStartTimeEpochMs(
                    dk.getCaThi().getThoiGianBatDau() != null
                            ? dk.getCaThi().getThoiGianBatDau().toEpochMilli()
                            : null
            );
            dto.setStatus(mapSessionStatus(dk.getCaThi().getTrangThai()));
            result.add(dto);
        }
        result.sort(Comparator.comparing(SessionDto::getId));
        return result;
    }

    @PostMapping("/sessions/{sessionId}/start")
    @Transactional
    public Map<String, Object> startSession(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "questions", required = false) Integer ignoredQuestions
    ) {
        SinhVien sv = requireSinhVien(studentId);
        Integer caThiId = toInt(sessionId, "sessionId");
        DangKyThi dk = dangKyThiRepository.findByCaThiMaAndSinhVienMa(caThiId, sv.getMa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Sinh viên chưa được đăng ký ca thi này"));
        if (daHoanThanhCaThi(caThiId, sv.getMa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sinh viên đã hoàn thành ca thi này");
        }

        CaThi caThi = caThiRepository.findByMaWithDetails(caThiId);
        if (caThi == null || caThi.getDeThi() == null || caThi.getDeThi().getMa() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ca thi");
        }

        List<CauHoiDeThi> cauHoiDeThis = cauHoiDeThiRepository.findByDeThiMaForClient(caThi.getDeThi().getMa());
        ExamDto exam = toExamDto(caThi, cauHoiDeThis);
        messagingTemplate.convertAndSend("/topic/exam", exam);

        taoHoacCapNhatBaiThiDangLam(caThi, sv);

        return Map.of(
                "ok", true,
                "message", "Đã phát đề thi",
                "sessionId", dk.getCaThi().getMa()
        );
    }

    @PostMapping("/submit")
    @Transactional
    public Map<String, Object> submit(@RequestBody SubmitRequest request) {
        SinhVien sv = requireSinhVien(request.getStudentId());
        Integer caThiId = toInt(request.getExamId(), "examId");

        CaThi caThi = caThiRepository.findByMaWithDetails(caThiId);
        if (caThi == null || caThi.getDeThi() == null || caThi.getDeThi().getMa() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ca thi");
        }

        List<CauHoiDeThi> cauHoiDeThis = cauHoiDeThiRepository.findByDeThiMaForClient(caThi.getDeThi().getMa());
        Map<Integer, CauHoiDeThi> byQuestionId = new LinkedHashMap<>();
        for (CauHoiDeThi c : cauHoiDeThis) {
            byQuestionId.put(c.getMa(), c);
        }

        Map<Integer, Set<Integer>> selected = new HashMap<>();
        if (request.getAnswers() != null) {
            for (AnswerSubmit a : request.getAnswers()) {
                if (a.getQuestionId() == null) {
                    continue;
                }
                Integer questionId = toInt(a.getQuestionId(), "questionId");
                Set<Integer> selectedForQuestion = selected.computeIfAbsent(questionId, k -> new LinkedHashSet<>());
                if (a.getAnswerId() != null) {
                    selectedForQuestion.add(toInt(a.getAnswerId(), "answerId"));
                }
                if (a.getAnswerIds() != null) {
                    for (Long answerId : a.getAnswerIds()) {
                        if (answerId != null) {
                            selectedForQuestion.add(toInt(answerId, "answerIds"));
                        }
                    }
                }
            }
        }

        BaiThiSinhVien baiThi = layBaiThiMoiNhat(caThiId, sv.getMa());
        if (baiThi == null) {
            baiThi = BaiThiSinhVien.builder()
                    .caThi(caThi)
                    .sinhVien(sv)
                    .batDauLuc(Instant.now())
                    .tuDongNop(false)
                    .taoLuc(Instant.now())
                    .build();
        } else if (baiThi.getBatDauLuc() == null) {
            baiThi.setBatDauLuc(Instant.now());
        }
        baiThi.setNopBaiLuc(Instant.now());
        baiThi.setThoiGianLamBaiGiay(request.getTimeSpentSeconds());
        baiThi.setTrangThai(TrangThaiBaiThi.DA_NOP);
        baiThi = baiThiSinhVienRepository.save(baiThi);

        int correctCount = 0;
        BigDecimal tongDiemDatDuoc = BigDecimal.ZERO;
        BigDecimal tongDiemToiDa = BigDecimal.ZERO;
        List<Boolean> results = new ArrayList<>();
        for (CauHoiDeThi cauHoiDeThi : cauHoiDeThis) {
            Integer qid = cauHoiDeThi.getMa();
            Set<Integer> selectedAnswerIds = selected.getOrDefault(qid, Set.of());
            int soDapAnDungDuocChon = demSoDapAnDungDuocChon(cauHoiDeThi, selectedAnswerIds);
            int tongSoDapAnDung = demTongSoDapAnDung(cauHoiDeThi);
            BigDecimal diemCauHoi = layDiemCauHoi(cauHoiDeThi);
            BigDecimal diemDatDuoc = tinhDiemCauHoi(diemCauHoi, soDapAnDungDuocChon, tongSoDapAnDung);
            tongDiemDatDuoc = tongDiemDatDuoc.add(diemDatDuoc);
            tongDiemToiDa = tongDiemToiDa.add(diemCauHoi);
            boolean correct = laCauDungHoanToan(cauHoiDeThi, selectedAnswerIds);
            results.add(correct);
            if (correct) {
                correctCount++;
            }

            CauTraLoiSinhVien cauTraLoi = CauTraLoiSinhVien.builder()
                    .baiThiSinhVien(baiThi)
                    .cauHoiDeThi(cauHoiDeThi)
                    .laDapAnDung(correct)
                    .diemDatDuoc(diemDatDuoc)
                    .traLoiLuc(Instant.now())
                    .build();
            cauTraLoi = cauTraLoiSinhVienRepository.save(cauTraLoi);

            for (Integer selectedAnswerId : selectedAnswerIds) {
                LuaChonCauHoi luaChon = luaChonCauHoiRepository.findById(selectedAnswerId).orElse(null);
                if (luaChon != null) {
                    LuaChonCauTraLoi lctl = LuaChonCauTraLoi.builder()
                            .cauTraLoiSinhVien(cauTraLoi)
                            .luaChon(luaChon)
                            .build();
                    luaChonCauTraLoiRepository.save(lctl);
                }
            }
        }

        int totalQuestions = request.getTotalQuestions() != null
                ? request.getTotalQuestions()
                : cauHoiDeThis.size();
        double score = tinhDiemThang10(tongDiemDatDuoc, tongDiemToiDa);
        BigDecimal tongDiemThang10 = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
        baiThi.setTongDiem(tongDiemThang10);
        BigDecimal nguongDiemDat = caThi.getDeThi().getDiemDat();
        if (nguongDiemDat == null) {
            nguongDiemDat = BigDecimal.valueOf(4);
        }
        baiThi.setDat(tongDiemThang10.compareTo(nguongDiemDat) >= 0);
        baiThiSinhVienRepository.save(baiThi);

        return Map.of(
                "ok", true,
                "score", score,
                "correctCount", correctCount,
                "totalQuestions", totalQuestions,
                "timeSpentSeconds", request.getTimeSpentSeconds(),
                "durationSeconds", request.getDurationSeconds(),
                "results", results
        );
    }

    private boolean laCauDungHoanToan(CauHoiDeThi cauHoiDeThi, Set<Integer> selectedAnswerIds) {
        if (cauHoiDeThi.getCauHoi() == null || cauHoiDeThi.getCauHoi().getLuaChonCauHois() == null) {
            return false;
        }
        Set<Integer> dapAnDung = new HashSet<>();
        for (LuaChonCauHoi luaChon : cauHoiDeThi.getCauHoi().getLuaChonCauHois()) {
            if (luaChon.getMa() != null && Boolean.TRUE.equals(luaChon.getLaDapAnDung())) {
                dapAnDung.add(luaChon.getMa());
            }
        }
        if (dapAnDung.isEmpty()) {
            return false;
        }
        return dapAnDung.equals(selectedAnswerIds);
    }

    private int demTongSoDapAnDung(CauHoiDeThi cauHoiDeThi) {
        if (cauHoiDeThi.getCauHoi() == null || cauHoiDeThi.getCauHoi().getLuaChonCauHois() == null) {
            return 0;
        }
        int tong = 0;
        for (LuaChonCauHoi luaChon : cauHoiDeThi.getCauHoi().getLuaChonCauHois()) {
            if (Boolean.TRUE.equals(luaChon.getLaDapAnDung())) {
                tong++;
            }
        }
        return tong;
    }

    private int demSoDapAnDungDuocChon(CauHoiDeThi cauHoiDeThi, Set<Integer> selectedAnswerIds) {
        if (selectedAnswerIds == null || selectedAnswerIds.isEmpty()
                || cauHoiDeThi.getCauHoi() == null || cauHoiDeThi.getCauHoi().getLuaChonCauHois() == null) {
            return 0;
        }
        int soDung = 0;
        for (LuaChonCauHoi luaChon : cauHoiDeThi.getCauHoi().getLuaChonCauHois()) {
            if (luaChon.getMa() != null
                    && selectedAnswerIds.contains(luaChon.getMa())
                    && Boolean.TRUE.equals(luaChon.getLaDapAnDung())) {
                soDung++;
            }
        }
        return soDung;
    }

    private BigDecimal layDiemCauHoi(CauHoiDeThi cauHoiDeThi) {
        if (cauHoiDeThi.getDiem() != null) {
            return cauHoiDeThi.getDiem();
        }
        if (cauHoiDeThi.getCauHoi() != null && cauHoiDeThi.getCauHoi().getDiem() != null) {
            return cauHoiDeThi.getCauHoi().getDiem();
        }
        return BigDecimal.ONE;
    }

    private BigDecimal tinhDiemCauHoi(BigDecimal diemCauHoi, int soDungDuocChon, int tongSoDapAnDung) {
        if (diemCauHoi == null || tongSoDapAnDung <= 0 || soDungDuocChon <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal diemMoiDapAn = diemCauHoi.divide(BigDecimal.valueOf(tongSoDapAnDung), 4, RoundingMode.HALF_UP);
        BigDecimal diem = diemMoiDapAn.multiply(BigDecimal.valueOf(soDungDuocChon));
        if (diem.compareTo(diemCauHoi) > 0) {
            diem = diemCauHoi;
        }
        return diem.setScale(2, RoundingMode.HALF_UP);
    }

    private double tinhDiemThang10(BigDecimal tongDiemDatDuoc, BigDecimal tongDiemToiDa) {
        if (tongDiemToiDa == null || tongDiemToiDa.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal diem10 = tongDiemDatDuoc
                .divide(tongDiemToiDa, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN)
                .setScale(2, RoundingMode.HALF_UP);
        return diem10.doubleValue();
    }

    private ExamDto toExamDto(CaThi caThi, List<CauHoiDeThi> cauHoiDeThis) {
        ExamDto exam = new ExamDto();
        exam.setId(caThi.getMa() == null ? null : caThi.getMa().longValue());
        exam.setTitle(caThi.getTenCaThi());
        if (caThi.getDeThi() != null && caThi.getDeThi().getThoiLuongPhut() != null) {
            exam.setDurationSeconds(caThi.getDeThi().getThoiLuongPhut() * 60);
        }

        List<QuestionDto> questions = new ArrayList<>();
        for (CauHoiDeThi chdt : cauHoiDeThis) {
            if (chdt.getCauHoi() == null || chdt.getMa() == null) {
                continue;
            }
            QuestionDto q = new QuestionDto();
            q.setId(chdt.getMa().longValue());
            q.setContent(chdt.getCauHoi().getNoiDung());
            q.setQuestionType(
                    chdt.getCauHoi().getLoaiCauHoi() != null
                            ? chdt.getCauHoi().getLoaiCauHoi().name()
                            : null
            );
            q.setMaxSelectableAnswers(demTongSoDapAnDung(chdt));

            List<AnswerDto> answers = new ArrayList<>();
            List<LuaChonCauHoi> luaChons = chdt.getCauHoi().getLuaChonCauHois();
            if (luaChons != null) {
                luaChons.sort(Comparator.comparing(LuaChonCauHoi::getThuTuHienThi, Comparator.nullsLast(Integer::compareTo)));
                for (LuaChonCauHoi lc : luaChons) {
                    if (lc.getMa() == null) {
                        continue;
                    }
                    AnswerDto a = new AnswerDto();
                    a.setId(lc.getMa().longValue());
                    a.setContent(lc.getNoiDungLuaChon());
                    answers.add(a);
                }
            }
            q.setAnswers(answers);
            questions.add(q);
        }
        exam.setQuestions(questions);
        return exam;
    }

    private SinhVien requireSinhVien(Long studentId) {
        Integer id = toInt(studentId, "studentId");
        return sinhVienRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên"));
    }

    private Integer toInt(Long v, String field) {
        if (v == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu " + field);
        }
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " không hợp lệ");
        }
        return v.intValue();
    }

    private String mapSessionStatus(TrangThaiCaThi status) {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case CHO_DANG_KY -> "PENDING";
            case DANG_DIEN_RA -> "OPEN";
            case DA_KET_THUC -> "CLOSED";
            case DA_HUY -> "CANCELLED";
        };
    }

    private BaiThiSinhVien layBaiThiMoiNhat(Integer maCaThi, Integer maSinhVien) {
        List<BaiThiSinhVien> ds = baiThiSinhVienRepository
                .findByCaThiMaAndSinhVienMaOrderByMoiNhat(maCaThi, maSinhVien);
        return ds.isEmpty() ? null : ds.get(0);
    }

    private void taoHoacCapNhatBaiThiDangLam(CaThi caThi, SinhVien sv) {
        if (caThi == null || caThi.getMa() == null || sv == null || sv.getMa() == null) {
            return;
        }
        BaiThiSinhVien hienTai = layBaiThiMoiNhat(caThi.getMa(), sv.getMa());
        if (hienTai != null && hienTai.getTrangThai() == TrangThaiBaiThi.DANG_LAM) {
            return;
        }
        BaiThiSinhVien baiThi = BaiThiSinhVien.builder()
                .caThi(caThi)
                .sinhVien(sv)
                .batDauLuc(Instant.now())
                .trangThai(TrangThaiBaiThi.DANG_LAM)
                .tuDongNop(false)
                .taoLuc(Instant.now())
                .build();
        baiThiSinhVienRepository.save(baiThi);
    }

    private boolean daHoanThanhCaThi(Integer maCaThi, Integer maSinhVien) {
        return baiThiSinhVienRepository.existsByCaThiMaAndSinhVienMaAndTrangThaiIn(
                maCaThi,
                maSinhVien,
                List.of(TrangThaiBaiThi.DA_NOP, TrangThaiBaiThi.DA_CHAM)
        );
    }

    @Data
    private static class LoginRequest {
        private String mssv;
        private String password;
    }

    @Data
    private static class LoginResponse {
        private Long studentId;
        private String mssv;
        private String fullName;
    }

    @Data
    private static class SessionDto {
        private Long id;
        private String name;
        private String subject;
        private Integer durationSeconds;
        private String location;
        private Long startTimeEpochMs;
        private String status;
    }

    @Data
    private static class SubmitRequest {
        private Long studentId;
        private Long examId;
        private List<AnswerSubmit> answers;
        private Integer totalQuestions;
        private Integer timeSpentSeconds;
        private Integer durationSeconds;
    }

    @Data
    private static class AnswerSubmit {
        private Long questionId;
        private Long answerId;
        private List<Long> answerIds;
    }

    @Data
    private static class ExamDto {
        private Long id;
        private String title;
        private Integer durationSeconds;
        private List<QuestionDto> questions;
    }

    @Data
    private static class QuestionDto {
        private Long id;
        private String content;
        private String questionType;
        private Integer maxSelectableAnswers;
        private List<AnswerDto> answers;
    }

    @Data
    private static class AnswerDto {
        private Long id;
        private String content;
    }
}
