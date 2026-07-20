package com.rabbiter.em.storage.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储端口（Port）。
 * <p>
 * 所有文件上传/下载业务（头像、商品图、合同等）都应通过此端口，
 * 而不直接操作 {@code java.io.File}。
 * <p>
 * 切换存储后端（本地磁盘 → 阿里云 OSS → MinIO → 七牛云）时，
 * 只需新增 Adapter 实现并切换 {@code storage.backend} 配置。
 */
public interface StorageService {

    /**
     * 上传文件。
     *
     * @param file     Spring 上传对象
     * @param category 业务分类（如 "avatar"、"file"），决定存储子目录
     * @return 可访问的相对/绝对 URL（写入数据库）
     */
    String upload(MultipartFile file, String category);

    /**
     * 下载文件为字节数组。
     */
    byte[] download(String category, String fileName);

    /**
     * 删除文件。
     *
     * @return 是否删除成功
     */
    boolean delete(String category, String fileName);

    /**
     * 检查文件是否存在。
     */
    boolean exists(String category, String fileName);
}
