package org.ltdz.taskflow.service;

import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.common.IdGenerator;
import org.ltdz.taskflow.model.po.AssetPo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 附件存储服务：只负责磁盘文件读写，不碰数据库。
 * <p>
 * 目录规则：{root}/{taskflowId}/{taskId}/{assetId}.{suffix}
 * <p>
 * 全程流式处理：上传用 {@link InputStream} 直接落盘，下载返回 {@link FileSystemResource}，
 * 不会把整个文件读入虚拟机内存。
 */
@Slf4j
@Service
public class FileStorageService {

    /** 附件根目录（来自 application.yaml 的 taskflow.storage.root） */
    @Value("${taskflow.storage.root}")
    private String root;

    /**
     * 保存上传的文件，返回填好 title / suffix / address 的 AssetPo（不写库）。
     *
     * @param file       上传的文件
     * @param taskflowId 归属任务流 id（用于目录分层）
     * @param taskId     归属任务 id
     * @param isInput    1 输入文件，0 输出文件
     */
    public AssetPo save(MultipartFile file, Long taskflowId, Long taskId, int isInput) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        long assetId = IdGenerator.next();
        String originalName = file.getOriginalFilename();
        String title = (originalName == null || originalName.isBlank()) ? String.valueOf(assetId) : originalName;
        String suffix = extractSuffix(title);
        String fileName = suffix.isEmpty() ? String.valueOf(assetId) : assetId + "." + suffix;

        Path dir = taskDir(taskflowId, taskId);
        Path target = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            // 流式复制：边读边写，不在内存里整体持有文件
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("附件写入失败：" + fileName, e);
        }

        AssetPo asset = new AssetPo();
        asset.setId(assetId);
        asset.setTaskId(taskId);
        asset.setIsInput(isInput);
        asset.setTitle(title);
        asset.setSuffix(suffix);
        asset.setAddress(dir.toString());
        asset.setUpdateTime(LocalDateTime.now());
        return asset;
    }

    /**
     * 由 AssetPo 算出完整文件路径。
     */
    public Path resolve(AssetPo asset) {
        String fileName = (asset.getSuffix() == null || asset.getSuffix().isBlank())
                ? String.valueOf(asset.getId())
                : asset.getId() + "." + asset.getSuffix();
        return Paths.get(asset.getAddress()).resolve(fileName);
    }

    /**
     * 返回可流式输出的文件资源（不读入内存）。
     * <p>
     * 调用方直接放进 ResponseEntity 的 body，由 Spring 分块写出。
     */
    public Resource load(AssetPo asset) {
        Path path = resolve(asset);
        FileSystemResource resource = new FileSystemResource(path);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("附件文件不存在：" + path);
        }
        return resource;
    }

    /**
     * 删除单个附件文件。
     */
    public void delete(AssetPo asset) {
        Path path = resolve(asset);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除附件文件失败：{}", path, e);
        }
    }

    /**
     * 删除整个任务流目录（级联删除用）。
     */
    public void deleteByTaskflow(Long taskflowId) {
        Path dir = taskflowDir(taskflowId);
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            // 倒序：先删文件再删目录
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("删除附件路径失败：{}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("遍历附件目录失败：{}", dir, e);
        }
    }

    /**
     * 任务目录：{root}/{taskflowId}/{taskId}（绝对路径，已规范化）
     */
    private Path taskDir(Long taskflowId, Long taskId) {
        return Paths.get(root, String.valueOf(taskflowId), String.valueOf(taskId))
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 任务流目录：{root}/{taskflowId}（绝对路径，已规范化）
     */
    private Path taskflowDir(Long taskflowId) {
        return Paths.get(root, String.valueOf(taskflowId))
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 提取后缀（不含点，统一小写）；无后缀返回空字符串。
     */
    private String extractSuffix(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
