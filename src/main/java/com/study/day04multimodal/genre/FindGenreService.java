package com.study.day04multimodal.genre;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Service
public class FindGenreService {

    private final ChatClient chatClient;

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/wav",
            "audio/mpeg",
            "audio/mp3",
            "audio/x-wav"
    );

    public FindGenreService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public MusicGenreResult analyzeAudioGenre(MultipartFile file) {
        validateAudio(file);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());

        String prompt = """
            너는 음악 장르를 판단하는 음악 분석가다.
            첨부된 오디오를 듣고 가장 자연스러운 대표 장르와 세부 장르를 판단해라.

            파일명: %s

            판단 기준:
            - 오디오에서 실제로 들리는 리듬, 악기, 보컬/랩 방식, 편곡을 가장 중요하게 본다.
            - 파일명에 곡명이나 아티스트명이 보이면 보조 정보로 참고해도 된다.
            - 장르를 억지로 세분화하지 말고, 사람이 음악을 들었을 때 가장 자연스럽게 말할 장르를 고른다.
            - 유명곡이면 일반적으로 알려진 장르도 참고한다.
            - 확신이 낮거나 장르가 섞여 있으면 candidateGenres에 가까운 후보를 함께 넣는다.
            - mainGenre는 넓은 대표 장르, subGenre는 더 구체적인 장르로 쓴다.
            - candidateGenres는 가능성이 높은 순서로 3~5개를 넣고, 첫 번째 값은 subGenre와 같게 한다.
            - confidence는 0~100 정수로 쓴다.
            - description은 한국어 1~2문장으로, 왜 그렇게 판단했는지 짧게 쓴다.

            응답은 MusicGenreResult 구조에 맞춰라.
            """.formatted(nullToUnknown(file.getOriginalFilename()));

        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .media(mimeType, resource)
                )
                .call()
                .entity(MusicGenreResult.class);
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일을 읽는 중 오류가 발생했습니다.");
        }
    }

    private void validateAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오디오 파일을 업로드해 주세요.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "WAV 또는 MP3 파일만 지원합니다. 받은 타입: " + contentType);
        }
    }

    private String nullToUnknown(String value) {
        return value == null || value.isBlank() ? "알 수 없음" : value;
    }
}
