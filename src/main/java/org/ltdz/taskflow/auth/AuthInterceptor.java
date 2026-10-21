package org.ltdz.taskflow.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.ltdz.taskflow.common.ApiResponse;
import org.ltdz.taskflow.enums.ApiCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * JWT 鉴权拦截器：所有受保护请求先校验 Authorization 头。
 * <p>
 * 校验两件事：① token 合法（格式与签名）；② 当前仍在有效期内。
 * 任一不通过都返回 HTTP 401 + 统一响应体，前端据此清 token 并退回登录页。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 通过校验后，把用户 id 放进 request 属性，controller 可直接取用 */
    public static final String ATTR_USER_ID = "userId";

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            return reject(response, "缺少访问令牌");
        }

        String token = header.substring(PREFIX.length()).trim();
        if (token.isEmpty()) {
            return reject(response, "访问令牌为空");
        }

        try {
            JwtPayload payload = jwtService.parseToken(token);

            // 显式再校验一次有效期（解析时已校验，这里双保险）
            Date expireAt = payload.getExpireAt();
            if (expireAt == null || expireAt.before(new Date())) {
                return reject(response, "登录已过期");
            }

            request.setAttribute(ATTR_USER_ID, payload.getUserId());
            return true;
        } catch (Exception e) {
            return reject(response, "访问令牌无效或已过期");
        }
    }

    /**
     * 返回 HTTP 401 + 统一响应体（前端根据 HTTP 401 清 token 并退回登录页）。
     */
    private boolean reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String body = objectMapper.writeValueAsString(
                ApiResponse.builder()
                        .status(ApiCode.UNAUTHORIZED.getCode())
                        .message(message)
                        .build());
        response.getWriter().write(body);
        return false;
    }
}
