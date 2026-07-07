package com.study.day04multimodal.genre;

import java.util.List;

public record MusicGenreResult(
        String mainGenre,
        String subGenre,
        List<String> candidateGenres,
        Integer confidence,
        String description
) {}
