package com.example.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg",
            "mp4", "webm", "mov",
            "pdf", "doc", "docx", "ppt", "pptx", "md", "markdown", "txt"
    );

    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @Value("${fileBaseUrl:}")
    private String fileBaseUrl;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            validateFile(file);
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            String extension = getExtension(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "." + extension;
            Path target = safePath(fileName);
            FileUtil.writeBytes(file.getBytes(), target.toString());
            String url = fileBaseUrl + "/files/download/" + fileName;
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(e.getMessage() == null ? "文件上传失败" : e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            Path target = safePath(fileName);
            if (!target.toFile().isFile()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            byte[] bytes = FileUtil.readBytes(target.toString());
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.warn("文件下载失败：{}", fileName);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件不能超过50MB");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private String getExtension(String fileName) {
        if (StrUtil.isEmpty(fileName) || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("文件名不合法");
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new IllegalArgumentException("文件类型缺失");
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private Path safePath(String fileName) {
        Path base = Path.of(filePath).toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("文件路径不合法");
        }
        return target;
    }
}
