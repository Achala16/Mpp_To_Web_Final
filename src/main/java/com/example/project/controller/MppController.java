package com.example.project.controller;

import com.example.project.service.MppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mpp")
public class MppController {

    @Autowired
    private MppService mppService;

    @PostMapping("/upload")
    public String uploadMppFile(@RequestParam("file") MultipartFile file) {
        try {
            mppService.processMppFile(file);
            return "File processed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to process file!";
        }
    }
}
