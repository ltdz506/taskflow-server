package org.ltdz.taskflow.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 在线连接管理。
 * <p>
 * 维护双向映射：
 * <ul>
 *   <li>userId → 该用户的全部连接（推送时用）</li>
 *   <li>sessionId → userId（连接关闭时反查归属）</li>
 * </ul>
 */
@Slf4j
@Component
public class WsSessionManager {

    /** 单条连接发送超时（毫秒） */
    private static final int SEND_TIME_LIMIT_MS = 5_000;

    /** 单条连接发送缓冲区上限（字节） */
    private static final int BUFFER_SIZE_LIMIT = 512 * 1024;

    /** userId → 该用户的全部连接（一个用户可开多个标签页） */
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /** sessionId → userId，连接关闭时反查归属 */
    private final Map<String, Long> sessionOwners = new ConcurrentHashMap<>();

    /**
     * 登记一条新连接（双向登记）。
     * <p>
     * 存入时用 {@link ConcurrentWebSocketSessionDecorator} 包装：
     * 它内部保证同一连接串行发送，并限制发送时间与缓冲，无需手动 synchronized。
     */
    public void add(Long userId, WebSocketSession session) {
        sessionOwners.put(session.getId(), userId);
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
                session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(safeSession);
        log.debug("WebSocket 连接建立：userId={}, sessionId={}", userId, session.getId());
    }

    /**
     * 注销一条连接（双向清理）。
     * <p>
     * 先删 sessionOwners 拿到 userId，再删 userSessions；顺序不能反。
     */
    public void remove(WebSocketSession session) {
        Long userId = sessionOwners.remove(session.getId());
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            // 集合里存的是装饰器，按 sessionId 匹配移除
            sessions.removeIf(s -> s.getId().equals(session.getId()));
            if (sessions.isEmpty()) {
                // 空集合直接删掉，避免 userId 越积越多
                userSessions.remove(userId);
            }
        }
        log.debug("WebSocket 连接关闭：userId={}, sessionId={}", userId, session.getId());
    }

    /**
     * 给某个用户的全部连接推送消息；用户不在线时静默跳过。
     */
    public void sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.warn("WebSocket 推送失败，移除连接：userId={}, sessionId={}", userId, session.getId(), e);
                remove(session);
            }
        }
    }
}
