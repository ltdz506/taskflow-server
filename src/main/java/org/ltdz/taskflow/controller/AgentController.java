package org.ltdz.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.AuthInterceptor;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.ltdz.taskflow.model.bo.AssetFileBo;
import org.ltdz.taskflow.model.dto.RespondToTaskDto;
import org.ltdz.taskflow.model.vo.TaskSettledNowVo;
import org.ltdz.taskflow.model.vo.TokenVo;
import org.ltdz.taskflow.model.vo.UnsettledTaskflowVo;
import org.ltdz.taskflow.service.AgentService;
import org.ltdz.taskflow.service.AssetService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * Agent 端接口。
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final AssetService assetService;

    /**
     * 换取 token（无鉴权）。
     */
    @GetMapping("/getUserIllustration")
    public ApiResponse<TokenVo> getUserIllustration(@RequestParam("password") String password) {
        try {
            TokenVo vo = new TokenVo();
            vo.setToken(agentService.getUserIllustration(password));
            return ApiResponse.<TokenVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(vo)
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @GetMapping("/getUnsettledTaskflow")
    public ApiResponse<UnsettledTaskflowVo> getUnsettledTaskflow(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        try {
            return ApiResponse.<UnsettledTaskflowVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(agentService.getUnsettledTaskflow(userId))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @GetMapping("/getTaskSettledNowById")
    public ApiResponse<TaskSettledNowVo> getTaskSettledNowById(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("id") Long id) {
        try {
            return ApiResponse.<TaskSettledNowVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(agentService.getTaskSettledNowById(userId, id))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/respondToTask")
    public ApiResponse<Void> respondToTask(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestBody RespondToTaskDto dto) {
        try {
            agentService.respondToTask(userId, dto);
            return ApiResponse.<Void>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/uploadFileById")
    public ApiResponse<Void> uploadFileById(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("taskId") Long taskId,
            @RequestParam("file") MultipartFile file) {
        try {
            assetService.uploadOutputAsset(userId, taskId, file);
            return ApiResponse.<Void>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    /**
     * 下载文件：成功返回文件流，失败返回统一响应体。
     */
    @GetMapping("/getFileById")
    public ResponseEntity<?> getFileById(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("id") Long id) {
        try {
            AssetFileBo file = assetService.download(userId, id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.getTitle()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getResource());
        } catch (Exception e) {
            return ResponseEntity.ok(fail(e));
        }
    }

    @PostMapping("/destroyTaskflowById")
    public ApiResponse<Void> destroyTaskflowById(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("id") Long id) {
        try {
            agentService.destroyTaskflowById(userId, id);
            return ApiResponse.<Void>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    private String contentDisposition(String filename) {
        return ContentDisposition.attachment()
                .filename(filename == null || filename.isBlank() ? "download" : filename,
                        StandardCharsets.UTF_8)
                .build()
                .toString();
    }

    private <T> ApiResponse<T> fail(Exception e) {
        return ApiResponse.<T>builder()
                .status(ApiCode.PARAM_ERROR.getCode())
                .message(e.getMessage())
                .build();
    }
}
