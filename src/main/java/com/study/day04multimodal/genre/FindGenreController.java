package com.study.day04multimodal.genre;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/genre")
public class FindGenreController {

    private final FindGenreService findGenreService;

    public FindGenreController(FindGenreService findGenreService) {
        this.findGenreService = findGenreService;
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/genre/upload";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("genreResult", null);
        return "genre/upload";
    }

    @PostMapping("/analyze")
    public String analyzeMusic(@RequestParam("file") MultipartFile file, Model model) {
        MusicGenreResult musicGenreResult = findGenreService.analyzeAudioGenre(file);
        model.addAttribute("genreResult", musicGenreResult);
        return "genre/upload";
    }
}