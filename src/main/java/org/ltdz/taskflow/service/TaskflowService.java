package org.ltdz.taskflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.common.IdGenerator;
import org.ltdz.taskflow.enums.TaskStatus;
import org.ltdz.taskflow.enums.TaskflowStatus;
import org.ltdz.taskflow.mapper.AiResponseMapper;
import org.ltdz.taskflow.mapper.AssetMapper;
import org.ltdz.taskflow.mapper.TaskMapper;
import org.ltdz.taskflow.mapper.TaskflowMapper;
import org.ltdz.taskflow.model.bo.TaskItemBo;
import org.ltdz.taskflow.model.dto.CreateTaskflowDto;
import org.ltdz.taskflow.model.po.AiResponsePo;
import org.ltdz.taskflow.model.po.AssetPo;
import org.ltdz.taskflow.model.po.TaskPo;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.model.vo.AiResponseVo;
import org.ltdz.taskflow.model.vo.CreateTaskflowVo;
import org.ltdz.taskflow.model.vo.TaskAndResponseVo;
import org.ltdz.taskflow.model.vo.TaskflowDetailVo;
import org.ltdz.taskflow.model.vo.TaskflowListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户端任务流服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskflowService {

    private static final int PAGE_SIZE = 5;
    private static final int OUTPUT_ASSET = 0;

    private final TaskflowMapper taskflowMapper;
    private final TaskMapper taskMapper;
    private final AiResponseMapper aiResponseMapper;
    private final AssetMapper assetMapper;
    private final FileStorageService fileStorageService;
    private final ConfigService configService;
    private final ModelConverter converter;

    /**
     * 任务流列表（创建时间降序，每页5条）。
     */
    public TaskflowListVo getTaskflows(Long userId, String keyword, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<TaskflowPo> pos = taskflowMapper.selectPage(userId, keyword, offset, PAGE_SIZE);

        TaskflowListVo vo = new TaskflowListVo();
        vo.setTotal(taskflowMapper.countByUser(userId, keyword));
        vo.setList(pos.stream().map(converter::toTaskflowVo).toList());
        return vo;
    }

    /**
     * 任务流详情（对话形式）。
     */
    public TaskflowDetailVo getTaskflowDetailById(Long userId, Long id) {
        TaskflowPo taskflow = requireOwned(userId, id);

        List<TaskPo> tasks = taskMapper.selectByTaskflowId(id);
        List<Long> taskIds = tasks.stream().map(TaskPo::getId).toList();

        Map<Long, AiResponsePo> responseByTask = taskIds.isEmpty()
                ? Map.of()
                : aiResponseMapper.selectByTaskIds(taskIds).stream()
                .collect(Collectors.toMap(AiResponsePo::getTaskId, Function.identity()));

        Map<Long, List<AssetPo>> assetsByTask = taskIds.isEmpty()
                ? Map.of()
                : assetMapper.selectByTaskIds(taskIds).stream()
                .collect(Collectors.groupingBy(AssetPo::getTaskId));

        List<TaskAndResponseVo> pairs = new ArrayList<>();
        for (TaskPo task : tasks) {
            TaskAndResponseVo pair = new TaskAndResponseVo();
            pair.setTask(converter.toTaskVo(task));

            AiResponsePo response = responseByTask.get(task.getId());
            if (response != null) {
                AiResponseVo responseVo = converter.toAiResponseVo(response);
                responseVo.setAssets(assetsByTask.getOrDefault(task.getId(), List.of()).stream()
                        .filter(a -> a.getIsInput() != null && a.getIsInput() == OUTPUT_ASSET)
                        .map(converter::toAssetVo)
                        .toList());
                pair.setResponse(responseVo);
            }
            pairs.add(pair);
        }

        TaskflowDetailVo vo = new TaskflowDetailVo();
        vo.setTaskflowId(taskflow.getId());
        vo.setStartPrompt(taskflow.getStartPrompt());
        vo.setTaskAndResponses(pairs);
        return vo;
    }

    /**
     * 创建任务流（附件由前端拿到 taskIds 后再逐个上传）。
     */
    @Transactional
    public CreateTaskflowVo createTaskflow(Long userId, CreateTaskflowDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("任务流标题不能为空");
        }
        if (dto.getTasks() == null || dto.getTasks().isEmpty()) {
            throw new IllegalArgumentException("任务流至少需要 1 个任务");
        }

        int maxTasks = configService.getIntValue(userId, ConfigService.CONTENT_MAX_TASKS, 20);
        if (dto.getTasks().size() > maxTasks) {
            throw new IllegalArgumentException("任务数不能超过 " + maxTasks + " 个");
        }

        LocalDateTime now = LocalDateTime.now();
        long taskflowId = IdGenerator.next();

        TaskflowPo taskflow = new TaskflowPo();
        taskflow.setId(taskflowId);
        taskflow.setUserId(userId);
        taskflow.setTitle(dto.getTitle().trim());
        taskflow.setStartPrompt(dto.getStartPrompt());
        taskflow.setStatus(TaskflowStatus.EDIT.name());
        taskflow.setCreateTime(now);
        taskflowMapper.insert(taskflow);

        List<TaskPo> tasks = new ArrayList<>();
        List<Long> taskIds = new ArrayList<>();
        int order = 1;
        for (TaskItemBo item : dto.getTasks()) {
            if (item.getTitle() == null || item.getTitle().isBlank()) {
                throw new IllegalArgumentException("任务标题不能为空");
            }
            long taskId = IdGenerator.next();
            TaskPo task = new TaskPo();
            task.setId(taskId);
            task.setTaskflowId(taskflowId);
            task.setTitle(item.getTitle().trim());
            task.setRequest(item.getRequest());
            task.setOrder(order++);
            task.setStatus(TaskStatus.CREATED.name());
            tasks.add(task);
            taskIds.add(taskId);
        }
        taskMapper.insertBatch(tasks);

        CreateTaskflowVo vo = new CreateTaskflowVo();
        vo.setTaskflowId(taskflowId);
        vo.setTaskIds(taskIds);
        return vo;
    }

    /**
     * 删除任务流（仅编辑态，硬删除 + 级联）。
     */
    @Transactional
    public void deleteTaskflow(Long userId, Long id) {
        TaskflowPo taskflow = requireOwned(userId, id);
        if (!TaskflowStatus.EDIT.name().equals(taskflow.getStatus())) {
            throw new IllegalArgumentException("仅编辑状态的任务流可删除");
        }

        List<Long> taskIds = taskMapper.selectIdsByTaskflowId(id);
        if (!taskIds.isEmpty()) {
            aiResponseMapper.deleteByTaskIds(taskIds);
            assetMapper.deleteByTaskIds(taskIds);
        }
        taskMapper.deleteByTaskflowId(id);
        taskflowMapper.deleteById(id);

        // 磁盘文件（内部已捕获异常，不影响事务）
        fileStorageService.deleteByTaskflow(id);
    }

    private TaskflowPo requireOwned(Long userId, Long id) {
        TaskflowPo taskflow = taskflowMapper.selectById(id);
        if (taskflow == null || !Objects.equals(taskflow.getUserId(), userId)) {
            throw new IllegalArgumentException("任务流不存在");
        }
        return taskflow;
    }
}
