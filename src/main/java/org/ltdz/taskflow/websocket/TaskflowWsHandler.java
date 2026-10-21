package org.ltdz.taskflow.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.ltdz.taskflow.auth.JwtPayload;
import org.ltdz.taskflow.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.Map;

/**
 * WebSocket 连接处理器：连接生命周期管理。
 * <p>
 * 握手鉴权由内部类 {@link TokenHandshakeInterceptor} 完成。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskflowWsHandler extends TextWebSocketHandler {

    /** 握手成功后放进 attributes 的键 */
    public static final String ATTR_USER_ID = "userId";

    private final WsSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (userId == null) {
            // 理论上不会发生（握手拦截器已保证），兜底关闭
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessionManager.add(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 用户端目前不发送业务消息，仅记录日志（心跳由 WebSocket 协议层处理）
        log.debug("收到 WebSocket 消息：sessionId={}, payload={}", session.getId(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        log.warn("WebSocket 传输异常：sessionId={}", session.getId(), exception);
        closeQuietly(session, CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessionManager.remove(session);
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // 关闭失败无需处理
        }
    }

    /**
     * 握手拦截器：从 ?token= 校验 JWT，通过后把 userId 放进 attributes。
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class TokenHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtService jwtService;

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                       @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
            String token = UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("token");

            if (token == null || token.isBlank()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            try {
                JwtPayload payload = jwtService.parseToken(token);
                Date expireAt = payload.getExpireAt();
                if (expireAt == null || expireAt.before(new Date())) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
                attributes.put(ATTR_USER_ID, payload.getUserId());
                return true;
            } catch (Exception e) {
                log.warn("WebSocket 握手鉴权失败：{}", e.getMessage());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        @Override
        public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, Exception exception) {
            // 无需处理
        }
    }
}
