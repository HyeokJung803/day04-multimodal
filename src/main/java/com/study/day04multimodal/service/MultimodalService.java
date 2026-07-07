package com.study.day04multimodal.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Service
public class MultimodalService {

    private final ChatClient chatClient;

    // 허용 이미지 타입 JPG, PNG
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            MimeTypeUtils.IMAGE_JPEG_VALUE, MimeTypeUtils.IMAGE_PNG_VALUE
    );

    public MultimodalService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String analyzeImage(MultipartFile file, String conversationId) {
        validateImage(file);                            // 적합한 이미지인지 검증
        ByteArrayResource resource = toResource(file);  // 리소스 변환
        MimeType mimeType = MimeType.valueOf(file.getContentType()); // 컨텐츠 타입 추출

        return chatClient.prompt()
                .user(u -> u.text("업로드된 영수증 이미지에서 상호명, 총금액, 날짜, 구매항목을 추출해주세요.")
                        .media(mimeType, resource)
                )
                .call()
                .content();
    }

    // 멀티파트 파일 -> 리소스로 변경하는 메서드
    private static ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 리소스 읽는 중 오류 발생하였습니다.");
        }
    }

    // 검증
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일 업로드해주세요.");
        }
        String contentType = file.getContentType();  // 파일 가져오기

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "JPEG나, PNG 이미지만 지원합니다. 받은 타입 :" + contentType);
        }
    }





}
