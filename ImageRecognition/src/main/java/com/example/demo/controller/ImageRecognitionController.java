package com.example.demo.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.RecognitionData;
import com.example.demo.service.ImageRecognitionService;

@Controller
public class ImageRecognitionController {

    private static final String MODEL_PATH = "path/to/your/model.pb";
    private static final String LABELS_PATH = "path/to/your/labels.txt";

    private final ImageRecognitionService recognitionService;

    public ImageRecognitionController(ImageRecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @GetMapping("/")
    public String showForm() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Perform image recognition
            RecognitionData recognitionData = recognitionService.recognizeImage(file);

            // Store data in MongoDB
            recognitionService.storeRecognitionData(recognitionData);

            // Display results on the web page
            model.addAttribute("label", recognitionData.getLabel());
            model.addAttribute("score", recognitionData.getScore());
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing the image");
        }

        return "result";
    }
}