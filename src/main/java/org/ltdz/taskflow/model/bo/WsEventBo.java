package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * WebSocket 推送消息外壳：{event, time, data}。
 *
 * @param <T> 具体事件的载荷类型
 */
@Data
public class WsEventBo<T> {

    /** 事件名，如 task.responded */
    private String event;

    /** 推送时间（yyyy-MM-dd HH:mm:ss） */
    private String time;

    /** 事件载荷 */
    private T data;
}
