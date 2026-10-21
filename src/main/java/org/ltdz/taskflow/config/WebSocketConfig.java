package org.ltdz.taskflow.config;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.JwtService;
import org.ltdz.taskflow.websocket.TaskflowWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置：注册 /ws 端点，并挂上握手鉴权拦截器。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TaskflowWsHandler taskflowWsHandler;
    private final JwtService jwtService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(taskflowWsHandler, "/ws")
                .addInterceptors(new TaskflowWsHandler.TokenHandshakeInterceptor(jwtService))
                // 开发期允许跨域；上线时收紧为具体前端域名
                .setAllowedOriginPatterns("*");
    }
}
