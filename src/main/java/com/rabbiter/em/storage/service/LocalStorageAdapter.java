package com.rabbiter.em.storage.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地磁盘存储适配器（默认实现）。
 * <p>
 * 通过 application.yml 中 {@code storage.local.base-path} 配置根目录。
 * 开发环境默认指向项目根目录的 avatar/ 和 file/，行为与重构前一致。
 */
@Component
@ConditionalOnProperty(prefix = "storage", name = "backend", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StorageService {

    private final String basePath;

    public LocalStorageAdapter(@Value("${storage.local.base-path:./}") String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    @Override
    public String upload(MultipartFile file, String category) {
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1)
                : "";
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String finalName = ext.isEmpty() ? uuid : uuid + "." + ext;

        File targetDir = Paths.get(basePath, category).toFile();
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IllegalStateException("Cannot create dir: " + targetDir.getAbsolutePath());
        }
        File target = new File(targetDir, finalName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Upload failed", e);
        }
        return "/" + category + "/" + finalName;
    }

    @Override
    public byte[] download(String category, String fileName) {
        File f = Paths.get(basePath, category, fileName).toFile();
        if (!f.exists()) {
            throw new IllegalStateException("File not found: " + f.getAbsolutePath());
        }
        return FileUtil.readBytes(f);
    }

    @Override
    public boolean delete(String category, String fileName) {
        File f = Paths.get(basePath, category, fileName).toFile();
        return f.exists() && f.delete();
    }

    @Override
    public boolean exists(String category, String fileName) {
        return Paths.get(basePath, category, fileName).toFile().exists();
    }

    /**
     * 工具方法：计算 InputStream 的 MD5，用于去重。
     * 业务层调用而非重复实现。
     */
    public static String md5(InputStream in) {
        return SecureUtil.md5(in);
    }

    /**
     * 工具方法：拼接本地绝对路径。
     */
    public Path resolve(String category, String fileName) {
        return Paths.get(basePath, category, fileName);
    }
}
