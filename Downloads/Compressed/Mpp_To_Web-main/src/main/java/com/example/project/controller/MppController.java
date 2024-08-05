package com.example.project.controller;

import com.example.project.model.Project;
import com.example.project.service.MppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/mpp")
public class MppController {

    @Autowired
    private MppService mppService;

    private static final Logger logger = Logger.getLogger(MppController.class.getName());

    @PostMapping("/upload")
    public Project uploadMppFile(@RequestParam("file") MultipartFile file) {
        try {
            // Convert MultipartFile to File
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);

            return mppService.processMppFile(convFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error uploading MPP file", e);
            throw new RuntimeException("Error uploading MPP file", e);
        }
    }
}
