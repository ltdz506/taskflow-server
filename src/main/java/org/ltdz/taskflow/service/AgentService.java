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
import org.ltdz.taskflow.model.bo.TaskRespondedBo;
import org.ltdz.taskflow.model.bo.TaskRunningBo;
import org.ltdz.taskflow.model.bo.TaskflowCompletedBo;
import org.ltdz.taskflow.model.bo.TaskflowDestroyedBo;
import org.ltdz.taskflow.model.bo.TaskflowStartedBo;
import org.ltdz.taskflow.model.dto.RespondToTaskDto;
import org.ltdz.taskflow.model.po.AiResponsePo;
import org.ltdz.taskflow.model.po.AssetPo;
import org.ltdz.taskflow.model.po.TaskPo;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.model.vo.TaskSettledNowVo;
import org.ltdz.taskflow.model.vo.TaskVo;
import org.ltdz.taskflow.model.vo.TaskflowVo;
import org.ltdz.taskflow.model.vo.UnsettledTaskflowVo;
import org.ltdz.taskflow.websocket.WsEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Agent 端服务：换取 token、领取任务流、取任务、回复、销毁。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    /** 1 输入文件，0 输出文件 */
    private static final int INPUT_ASSET = 1;

    private final TaskflowMapper taskflowMapper;
    private final TaskMapper taskMapper;
    private final AiResponseMapper aiResponseMapper;
    private final AssetMapper assetMapper;
    private final AuthService authService;
    private final WsEventPublisher wsEventPublisher;
    private final ModelConverter converter;

    /**
     * 换取 token（接口无鉴权，凭访问密钥换）。
     */
    public String getUserIllustration(String password) {
        return authService.login(password);
    }

    /**
     * 领取待处理任务流：EDIT → RUNNING + 写 startTime，全部任务 CREATED → WAITING。
     */
    @Transactional
    public UnsettledTaskflowVo getUnsettledTaskflow(Long userId) {
        // 防御：已有启动中的任务流时不再领取（正常调用顺序下不会出现）
        if (taskflowMapper.countByStatus(userId, TaskflowStatus.RUNNING.name()) > 0) {
            return emptyUnsettled();
        }

        TaskflowPo taskflow = taskflowMapper.selectEarliestByStatus(userId, TaskflowStatus.EDIT.name());
        if (taskflow == null) {
            return emptyUnsettled();
        }

        LocalDateTime now = LocalDateTime.now();
        taskflowMapper.updateStatusAndStartTime(taskflow.getId(), TaskflowStatus.RUNNING.name(), now);
        int waiting = taskMapper.updateStatusByTaskflowId(
                taskflow.getId(), TaskStatus.CREATED.name(), TaskStatus.WAITING.name());
        log.info("任务流 {} 已启动，{} 个任务转为等待", taskflow.getId(), waiting);

        TaskflowStartedBo startedBo = new TaskflowStartedBo();
        startedBo.setTaskflowId(taskflow.getId());
        startedBo.setStatus(TaskflowStatus.RUNNING.name());
        startedBo.setStartTime(converter.format(now));
        wsEventPublisher.taskflowStarted(userId, startedBo);

        TaskflowVo taskflowVo = converter.toTaskflowVo(taskflow);
        taskflowVo.setStatus(TaskflowStatus.RUNNING.name());
        taskflowVo.setStartTime(converter.format(now));

        UnsettledTaskflowVo vo = new UnsettledTaskflowVo();
        vo.setTaskflow(taskflowVo);
        vo.setHasMore(taskflowMapper.countByStatus(userId, TaskflowStatus.EDIT.name()) > 0);
        return vo;
    }

    /**
     * 取当前任务：第一个 WAITING → RUNNING + 写 settleTime。
     */
    @Transactional
    public TaskSettledNowVo getTaskSettledNowById(Long userId, Long taskflowId) {
        TaskflowPo taskflow = requireOwned(userId, taskflowId);
        if (!TaskflowStatus.RUNNING.name().equals(taskflow.getStatus())) {
            throw new IllegalArgumentException("任务流未处于启动状态");
        }

        TaskPo task = taskMapper.selectFirstByStatus(taskflowId, TaskStatus.WAITING.name());
        if (task == null) {
            TaskSettledNowVo empty = new TaskSettledNowVo();
            empty.setTask(null);
            empty.setAssets(List.of());
            empty.setHasMore(false);
            return empty;
        }

        LocalDateTime now = LocalDateTime.now();
        taskMapper.updateStatusAndSettleTime(task.getId(), TaskStatus.RUNNING.name(), now);

        List<AssetPo> assets = assetMapper.selectByTaskId(task.getId());

        TaskPo last = taskMapper.selectLastByTaskflowId(taskflowId);
        boolean hasMore = last != null && !Objects.equals(last.getId(), task.getId());

        TaskRunningBo runningBo = new TaskRunningBo();
        runningBo.setTaskflowId(taskflow.getId());
        runningBo.setTaskId(task.getId());
        runningBo.setStatus(TaskStatus.RUNNING.name());
        runningBo.setSettleTime(converter.format(now));
        wsEventPublisher.taskRunning(userId, runningBo);

        TaskVo taskVo = converter.toTaskVo(task);
        taskVo.setStatus(TaskStatus.RUNNING.name());
        taskVo.setSettleTime(converter.format(now));
        taskVo.setAssets(assets.stream()
                .filter(a -> a.getIsInput() != null && a.getIsInput() == INPUT_ASSET)
                .map(converter::toAssetVo)
                .toList());

        TaskSettledNowVo vo = new TaskSettledNowVo();
        vo.setTask(taskVo);
        vo.setAssets(assets.stream().map(converter::toAssetVo).toList());
        vo.setHasMore(hasMore);
        return vo;
    }

    /**
     * 回复任务：RUNNING → TERMINATED 并写回复；若为最后一个任务，任务流 → COMPLETED。
     */
    @Transactional
    public void respondToTask(Long userId, RespondToTaskDto dto) {
        if (dto.getTaskId() == null) {
            throw new IllegalArgumentException("任务 id 不能为空");
        }
        if (dto.getResponse() == null) {
            throw new IllegalArgumentException("回复内容不能为空");
        }

        TaskPo task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        TaskflowPo taskflow = requireOwned(userId, task.getTaskflowId());
        if (!TaskflowStatus.RUNNING.name().equals(taskflow.getStatus())) {
            throw new IllegalArgumentException("任务流未处于启动状态");
        }
        if (!TaskStatus.RUNNING.name().equals(task.getStatus())) {
            throw new IllegalArgumentException("任务不在运行状态");
        }

        LocalDateTime now = LocalDateTime.now();
        // 只改状态，settleTime 保留原值
        taskMapper.updateStatusAndSettleTime(task.getId(), TaskStatus.TERMINATED.name(), task.getSettleTime());

        AiResponsePo response = new AiResponsePo();
        response.setId(IdGenerator.next());
        response.setTaskId(task.getId());
        response.setResponse(dto.getResponse());
        response.setRespTime(now);
        aiResponseMapper.insert(response);

        TaskRespondedBo respondedBo = new TaskRespondedBo();
        respondedBo.setTaskflowId(taskflow.getId());
        respondedBo.setTaskId(task.getId());
        respondedBo.setStatus(TaskStatus.TERMINATED.name());
        respondedBo.setResponse(dto.getResponse());
        respondedBo.setRespTime(converter.format(now));
        wsEventPublisher.taskResponded(userId, respondedBo);

        TaskPo last = taskMapper.selectLastByTaskflowId(taskflow.getId());
        if (last != null && Objects.equals(last.getId(), task.getId())) {
            taskflowMapper.updateStatusAndEndTime(taskflow.getId(), TaskflowStatus.COMPLETED.name(), now);

            TaskflowCompletedBo completedBo = new TaskflowCompletedBo();
            completedBo.setTaskflowId(taskflow.getId());
            completedBo.setStatus(TaskflowStatus.COMPLETED.name());
            completedBo.setEndTime(converter.format(now));
            wsEventPublisher.taskflowCompleted(userId, completedBo);
        }
    }

    /**
     * 销毁任务流：RUNNING → DESTROYED + 写 endTime，**不改任务状态**。
     */
    @Transactional
    public void destroyTaskflowById(Long userId, Long id) {
        TaskflowPo taskflow = requireOwned(userId, id);
        if (!TaskflowStatus.RUNNING.name().equals(taskflow.getStatus())) {
            throw new IllegalArgumentException("仅启动状态的任务流可销毁");
        }

        LocalDateTime now = LocalDateTime.now();
        taskflowMapper.updateStatusAndEndTime(id, TaskflowStatus.DESTROYED.name(), now);

        TaskflowDestroyedBo destroyedBo = new TaskflowDestroyedBo();
        destroyedBo.setTaskflowId(id);
        destroyedBo.setStatus(TaskflowStatus.DESTROYED.name());
        destroyedBo.setEndTime(converter.format(now));
        wsEventPublisher.taskflowDestroyed(userId, destroyedBo);
    }

    private UnsettledTaskflowVo emptyUnsettled() {
        UnsettledTaskflowVo vo = new UnsettledTaskflowVo();
        vo.setTaskflow(null);
        vo.setHasMore(false);
        return vo;
    }

    private TaskflowPo requireOwned(Long userId, Long taskflowId) {
        TaskflowPo taskflow = taskflowMapper.selectById(taskflowId);
        if (taskflow == null || !Objects.equals(taskflow.getUserId(), userId)) {
            throw new IllegalArgumentException("任务流不存在");
        }
        return taskflow;
    }
}
