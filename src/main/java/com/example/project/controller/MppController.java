package com.example.project.controller;

import com.example.project.model.Project;
import com.example.project.service.MppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/mpp")
public class MppController {

    @Autowired
    private MppService mppService;

    @PostMapping("/upload")
    public Project uploadMppFile(@RequestParam("file") MultipartFile file) throws Exception {
        // Convert MultipartFile to File
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);

        return mppService.processMppFile(convFile);
    }
}
