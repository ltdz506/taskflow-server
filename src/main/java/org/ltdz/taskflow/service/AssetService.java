package org.ltdz.taskflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.enums.TaskflowStatus;
import org.ltdz.taskflow.mapper.AssetMapper;
import org.ltdz.taskflow.mapper.TaskMapper;
import org.ltdz.taskflow.mapper.TaskflowMapper;
import org.ltdz.taskflow.model.bo.AssetAddedBo;
import org.ltdz.taskflow.model.bo.AssetFileBo;
import org.ltdz.taskflow.model.po.AssetPo;
import org.ltdz.taskflow.model.po.TaskPo;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.model.vo.UploadAssetVo;
import org.ltdz.taskflow.websocket.WsEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 附件服务：用户端上传输入文件、Agent 上传输出文件、两端共用下载。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    /** 1 输入文件，0 输出文件 */
    private static final int INPUT_ASSET = 1;
    private static final int OUTPUT_ASSET = 0;

    private final AssetMapper assetMapper;
    private final TaskMapper taskMapper;
    private final TaskflowMapper taskflowMapper;
    private final FileStorageService fileStorageService;
    private final ConfigService configService;
    private final WsEventPublisher wsEventPublisher;
    private final ModelConverter converter;

    /**
     * 用户端上传输入文件（仅编辑态任务流；受附件数 / 文件大小设置项限制）。
     */
    public UploadAssetVo uploadInputAsset(Long userId, Long taskId, MultipartFile file) {
        TaskPo task = requireTask(taskId);
        TaskflowPo taskflow = requireTaskflowOfUser(userId, task.getTaskflowId());

        if (!TaskflowStatus.EDIT.name().equals(taskflow.getStatus())) {
            throw new IllegalArgumentException("仅编辑状态的任务流可上传附件");
        }

        int maxAssets = configService.getIntValue(userId, ConfigService.CONTENT_MAX_ASSETS, 10);
        if (assetMapper.countByTaskId(taskId) >= maxAssets) {
            throw new IllegalArgumentException("单任务附件数不能超过 " + maxAssets + " 个");
        }

        int maxMb = configService.getIntValue(userId, ConfigService.CONTENT_MAX_FILE_SIZE, 100);
        if (file.getSize() > maxMb * 1024L * 1024L) {
            throw new IllegalArgumentException("文件大小不能超过 " + maxMb + " MB");
        }

        AssetPo asset = fileStorageService.save(file, taskflow.getId(), taskId, INPUT_ASSET);
        assetMapper.insert(asset);

        UploadAssetVo vo = new UploadAssetVo();
        vo.setId(asset.getId());
        vo.setTitle(asset.getTitle());
        vo.setSuffix(asset.getSuffix());
        return vo;
    }

    /**
     * Agent 上传输出文件，成功后推送 asset.added。
     */
    public void uploadOutputAsset(Long userId, Long taskId, MultipartFile file) {
        TaskPo task = requireTask(taskId);
        TaskflowPo taskflow = requireTaskflowOfUser(userId, task.getTaskflowId());

        AssetPo asset = fileStorageService.save(file, taskflow.getId(), taskId, OUTPUT_ASSET);
        assetMapper.insert(asset);

        AssetAddedBo bo = new AssetAddedBo();
        bo.setTaskflowId(taskflow.getId());
        bo.setTaskId(taskId);
        bo.setAsset(converter.toAssetVo(asset));
        bo.setUpdateTime(converter.format(asset.getUpdateTime()));
        wsEventPublisher.assetAdded(userId, bo);
    }

    /**
     * 下载附件（用户端与 Agent 端共用）。
     */
    public AssetFileBo download(Long userId, Long assetId) {
        AssetPo asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new IllegalArgumentException("附件不存在");
        }
        TaskPo task = requireTask(asset.getTaskId());
        requireTaskflowOfUser(userId, task.getTaskflowId());

        AssetFileBo bo = new AssetFileBo();
        bo.setResource(fileStorageService.load(asset));
        bo.setTitle(asset.getTitle());
        return bo;
    }

    private TaskPo requireTask(Long taskId) {
        TaskPo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return task;
    }

    private TaskflowPo requireTaskflowOfUser(Long userId, Long taskflowId) {
        TaskflowPo taskflow = taskflowMapper.selectById(taskflowId);
        if (taskflow == null || !Objects.equals(taskflow.getUserId(), userId)) {
            throw new IllegalArgumentException("任务流不存在");
        }
        return taskflow;
    }
}
