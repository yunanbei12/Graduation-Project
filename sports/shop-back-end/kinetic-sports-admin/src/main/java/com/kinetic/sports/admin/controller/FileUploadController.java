package com.kinetic.sports.admin.controller;

import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.common.util.ContentSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.url-prefix:/uploads}")
    private String urlPrefix;

    @PostMapping("/upload")
    public ServerResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ext = ContentSecurityUtils.validateAndResolveImageExtension(file);
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadDir, datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFilename);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            return ServerResponseEntity.fail("文件保存失败: " + e.getMessage());
        }

        String url = urlPrefix + "/" + datePath + "/" + newFilename;
        return ServerResponseEntity.success(url);
    }
}
