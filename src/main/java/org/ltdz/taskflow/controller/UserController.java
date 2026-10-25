package org.ltdz.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.AuthInterceptor;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.ltdz.taskflow.model.vo.UserVo;
import org.ltdz.taskflow.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户的信息（userId 由鉴权拦截器放入 request attribute）。
     */
    @GetMapping("/getUserInfo")
    public ApiResponse<UserVo> getUserInfo(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        try {
            return ApiResponse.<UserVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(userService.getUserInfo(userId))
                    .build();
        } catch (Exception e) {
            return ApiResponse.<UserVo>builder()
                    .status(ApiCode.PARAM_ERROR.getCode())
                    .message(e.getMessage())
                    .build();
        }
    }
}
