package org.ltdz.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.ltdz.taskflow.model.vo.TokenVo;
import org.ltdz.taskflow.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录：校验访问密钥，返回 token。
     */
    @GetMapping("/login")
    public ApiResponse<TokenVo> login(@RequestParam("password") String password) {
        try {
            TokenVo vo = new TokenVo();
            vo.setToken(authService.login(password));
            return ApiResponse.<TokenVo>builder()
                    .status(ApiCode.SUCCESS.getCode())
                    .message(ApiCode.SUCCESS.getMessage())
                    .data(vo)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<TokenVo>builder()
                    .status(ApiCode.PARAM_ERROR.getCode())
                    .message(e.getMessage())
                    .build();
        }
    }
}
