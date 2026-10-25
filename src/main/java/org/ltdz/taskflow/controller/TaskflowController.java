package org.ltdz.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.AuthInterceptor;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.ltdz.taskflow.model.bo.AssetFileBo;
import org.ltdz.taskflow.model.dto.CreateTaskflowDto;
import org.ltdz.taskflow.model.vo.CreateTaskflowVo;
import org.ltdz.taskflow.model.vo.TaskflowDetailVo;
import org.ltdz.taskflow.model.vo.TaskflowListVo;
import org.ltdz.taskflow.model.vo.UploadAssetVo;
import org.ltdz.taskflow.service.AssetService;
import org.ltdz.taskflow.service.TaskflowService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 用户端任务流接口。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class TaskflowController {

    private final TaskflowService taskflowService;
    private final AssetService assetService;

    @GetMapping("/getTaskflows")
    public ApiResponse<TaskflowListVo> getTaskflows(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        try {
            return ApiResponse.<TaskflowListVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(taskflowService.getTaskflows(userId, keyword, page))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @GetMapping("/getTaskflowDetailById")
    public ApiResponse<TaskflowDetailVo> getTaskflowDetailById(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("id") Long id) {
        try {
            return ApiResponse.<TaskflowDetailVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(taskflowService.getTaskflowDetailById(userId, id))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/createTaskflow")
    public ApiResponse<CreateTaskflowVo> createTaskflow(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestBody CreateTaskflowDto dto) {
        try {
            return ApiResponse.<CreateTaskflowVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(taskflowService.createTaskflow(userId, dto))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/deleteTaskflow")
    public ApiResponse<Void> deleteTaskflow(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("id") Long id) {
        try {
            taskflowService.deleteTaskflow(userId, id);
            return ApiResponse.<Void>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/uploadAsset")
    public ApiResponse<UploadAssetVo> uploadAsset(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("taskId") Long taskId,
            @RequestParam("file") MultipartFile file) {
        try {
            return ApiResponse.<UploadAssetVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(assetService.uploadInputAsset(userId, taskId, file))
                    .build();
        } catch (Exception e) {
            return fail(e);
        }
    }

    /**
     * 下载附件：成功返回文件流，失败返回统一响应体（HTTP 200 + status=400）。
     */
    @GetMapping("/downloadAsset")
    public ResponseEntity<?> downloadAsset(
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
