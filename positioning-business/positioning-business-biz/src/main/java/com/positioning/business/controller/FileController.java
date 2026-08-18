package com.positioning.business.controller;

import com.positioning.common.api.Result;
import com.positioning.common.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传接口（楼层平面图等, 管理后台使用）
 * <p>
 * 上传文件保存到本地 uploadDir（默认 ./uploads, 可用 FILE_UPLOAD_DIR 覆盖）,
 * 返回相对 URL（/uploads/xxx.png）, 经网关 /uploads/** 路由与静态资源映射可访问。
 * </p>
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传（楼层平面图等）, 返回可访问 URL")
@RestController
@RequestMapping("/business/file")
public class FileController {

    /** 上传目录（可环境变量覆盖） */
    @Value("${positioning.file.upload-dir:./uploads}")
    private String uploadDir;

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp", "gif", "svg");

    /** 上传文件（multipart/form-data, 字段名 file） */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传图片文件（楼层平面图等）, multipart/form-data 字段名 file, 返回 {\"url\":\"/uploads/xxx.png\"}")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "文件不能为空");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException(400, "仅支持图片类型: " + String.join("/", ALLOWED_EXT));
        }
        if (file.getContentType() != null && !file.getContentType().startsWith("image/")) {
            throw new BizException(400, "仅支持图片类型");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件上传成功: {} -> {}", original, target);
            return Result.ok(Map.of("url", "/uploads/" + filename));
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BizException(500, "文件保存失败");
        }
    }
}
