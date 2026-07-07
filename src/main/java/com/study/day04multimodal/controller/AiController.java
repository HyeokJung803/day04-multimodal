package com.study.day04multimodal.controller;

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
    public String imageAnalysis(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        return multimodalService.analyzeImage(file, conversationId);
    }
}
