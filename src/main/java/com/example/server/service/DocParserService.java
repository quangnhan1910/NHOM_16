package com.example.server.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service đọc nội dung file Word (.doc, .docx) và PDF (.pdf),
 * trả về chuỗi raw text giữ nguyên format câu hỏi.
 */
@Service
public class DocParserService {

    /**
     * Parse file thành raw text dựa trên extension.
     */
    public String parse(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();

        return switch (ext) {
            case ".docx" -> parseDocx(file.getInputStream());
            case ".doc" -> parseDoc(file.getInputStream());
            case ".pdf" -> parsePdf(file.getBytes());
            default -> throw new IllegalArgumentException(
                    "Định dạng file không được hỗ trợ: " + ext);
        };
    }

    /**
     * Đọc file .docx (Office Open XML) bằng Apache POI XWPFDocument.
     */
    private String parseDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                } else {
                    // Dòng trống → phân tách câu hỏi
                    sb.append("\n");
                }
            }
            return sb.toString().trim();
        }
    }

    /**
     * Đọc file .doc (OLE2) bằng Apache POI HWPFDocument.
     */
    private String parseDoc(InputStream is) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(doc)) {

            String[] paragraphs = extractor.getParagraphText();
            StringBuilder sb = new StringBuilder();

            for (String para : paragraphs) {
                String text = para.trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                } else {
                    sb.append("\n");
                }
            }
            return sb.toString().trim();
        }
    }

    /**
     * Đọc file .pdf bằng Apache PDFBox.
     */
    private String parsePdf(byte[] bytes) throws IOException {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc).trim();
        }
    }
}
