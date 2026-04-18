package com.kinetic.sports.admin.controller;

import com.kinetic.sports.common.response.ServerResponseEntity;
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
        if (file.isEmpty()) {
            return ServerResponseEntity.fail("文件不能为空");
        }

        // 生成文件路径：日期/UUID.扩展名
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 创建目录
        File dir = new File(uploadDir, datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File dest = new File(dir, newFilename);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            return ServerResponseEntity.fail("文件保存失败: " + e.getMessage());
        }

        // 返回可访问的URL
        String url = urlPrefix + "/" + datePath + "/" + newFilename;
        return ServerResponseEntity.success(url);
    }
}
