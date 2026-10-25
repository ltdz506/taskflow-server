package org.ltdz.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.AuthInterceptor;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.ltdz.taskflow.model.dto.UpdateUserConfigDto;
import org.ltdz.taskflow.model.vo.UserConfigVo;
import org.ltdz.taskflow.service.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设置项接口。
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 获取设置值：value 为 JSON 字符串（含控件描述），原样返回。
     */
    @GetMapping("/getUserConfig")
    public ApiResponse<UserConfigVo> getUserConfig(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        try {
            return ApiResponse.<UserConfigVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(configService.getUserConfig(userId))
                    .build();
        } catch (Exception e) {
            return ApiResponse.<UserConfigVo>builder()
                    .status(ApiCode.PARAM_ERROR.getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * 更新设置值：请求体只带 id-value 对，后端负责合并 JSON。
     */
    @PostMapping("/updateUserConfig")
    public ApiResponse<Void> updateUserConfig(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestBody UpdateUserConfigDto dto) {
        try {
            configService.updateUserConfig(userId, dto);
            return ApiResponse.<Void>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .status(ApiCode.PARAM_ERROR.getCode())
                    .message(e.getMessage())
                    .build();
        }
    }
}
