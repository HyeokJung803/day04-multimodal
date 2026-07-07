package com.study.day04multimodal.controller;

import com.study.day04multimodal.dto.PdfSummary;
import com.study.day04multimodal.dto.ReceiptInfo;
import com.study.day04multimodal.service.MultimodalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AiController {

    private final MultimodalService multimodalService;

    public AiController(MultimodalService multimodalService) {
        this.multimodalService = multimodalService;
    }

    @PostMapping("/api/image-analysis")
    public ReceiptInfo imageAnalysis(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        return multimodalService.analyzeImage(file, conversationId);
    }

    @PostMapping("/api/image-describe")
    public String imagedescribe(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        return multimodalService.describeImage(file, conversationId);
    }

    @PostMapping("/api/pdf-analysis")
    public PdfSummary pdfAnalysis(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        return multimodalService.analyzePdf(file, conversationId);
    }

    @PostMapping("/api/pdf-describe")
    public String pdfDescribe(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        return multimodalService.describePdf(file, conversationId);
    }
}
