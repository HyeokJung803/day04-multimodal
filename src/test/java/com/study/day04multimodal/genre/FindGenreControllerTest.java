package com.study.day04multimodal.genre;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FindGenreControllerTest {

    @Test
    void uploadFormReturnsUploadPage() {
        FindGenreService findGenreService = Mockito.mock(FindGenreService.class);
        FindGenreController controller = new FindGenreController(findGenreService);
        Model model = new ExtendedModelMap();

        assertEquals("genre/upload", controller.uploadForm(model));
    }

    @Test
    void analyzeMusicAddsResultToModel() {
        FindGenreService findGenreService = Mockito.mock(FindGenreService.class);
        FindGenreController controller = new FindGenreController(findGenreService);
        MusicGenreResult result = new MusicGenreResult(
                "HIP HOP/RAP",
                "Boom Bap Hip Hop",
                List.of("Boom Bap Hip Hop", "Jazz Hip Hop", "Alternative Hip Hop"),
                82,
                "샘플 기반 드럼과 랩 중심의 그루브가 두드러집니다."
        );
        when(findGenreService.analyzeAudioGenre(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.mp3",
                "audio/mpeg",
                "audio-data".getBytes()
        );
        Model model = new ExtendedModelMap();

        String viewName = controller.analyzeMusic(file, model);

        assertEquals("genre/upload", viewName);
        assertEquals(result, model.getAttribute("genreResult"));
    }
}
