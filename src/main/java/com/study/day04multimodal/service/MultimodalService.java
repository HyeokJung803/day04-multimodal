package com.study.day04multimodal.service;

import com.study.day04multimodal.dto.PdfSummary;
import com.study.day04multimodal.dto.ReceiptInfo;
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

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
        "audio/wav", "audio/mepg"
    );

    private static final String ALLOWED_PDF_TYPES = "application/pdf";

    public MultimodalService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public ReceiptInfo analyzeImage(MultipartFile file, String conversationId) {
        validateImage(file);                            // 적합한 이미지인지 검증
        ByteArrayResource resource = toResource(file);  // 리소스 변환
        MimeType mimeType = MimeType.valueOf(file.getContentType()); // 컨텐츠 타입 추출

        return chatClient.prompt()
                .user(u -> u.text("업로드된 영수증 이미지에서 상호명, 총금액, 날짜, 구매항목을 추출해주세요.")
                        .media(mimeType, resource)
                )
                .call()
                .entity(ReceiptInfo.class);
    }

    public String describeImage(MultipartFile file, String conversationId) {
        validateImage(file);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());

        String prompt = "입력받은 이미지가 무엇인지 친절하게 설명해주세요.";

        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .media(mimeType, resource)
                )
                .call()
                .content();
    }

    public PdfSummary analyzePdf(MultipartFile file, String conversationId) {
        validatePdf(file); // 검증
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());
        String prompt = "이 문서의 내용을 요약해주세요.";
        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .media(mimeType, resource)
                )
                .call()
                .entity(PdfSummary.class);
    }

    public String describePdf(MultipartFile file, String conversationId) {
        validatePdf(file); // 검증
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());
        String prompt = "이 문서의 내용을 요약해주세요.";
        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .media(mimeType, resource)
                )
                .call()
                .content();
    }

    public String describeAudio(MultipartFile file, String conversationId) {
        validateAudio(file);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());
        String prompt = "오디오 파일을 듣고 내용을 설명해주세요.";
        return chatClient.prompt()
                .user(u -> u.text(prompt)
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

    private void validateAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오디오 파일 업로드해주세요.");
        }
        String contentType = file.getContentType();  // 파일 가져오기

        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "WAV나, MP3만 지원합니다. 받은 타입 :" + contentType);
        }
    }
    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 파일 업로드해주세요.");
        }
        String contentType = file.getContentType();  // 파일 가져오기
        if (!contentType.equals(ALLOWED_PDF_TYPES)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PDF 파일만 지원합니다. 받은 타입 :" + contentType);
        }
    }





}
