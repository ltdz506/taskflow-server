package org.ltdz.taskflow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统一响应状态码。
 * <p>
 * 只保留项目实际用到的 4 个，后续需要再扩展。
 */
@Getter
@RequiredArgsConstructor
public enum ApiCode {

    /** 成功 */
    SUCCESS(200, "成功"),

    /** 接口因参数错误无法正常运行 **/
    PARAM_ERROR(400,"参数错误"),

    /** 未授权：token 缺失、无效或已过期，前端应退回登录页 */
    UNAUTHORIZED(401, "未授权，请重新登录"),

    /** 禁止访问：无权操作该资源 */
    FORBIDDEN(403, "禁止访问"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在");

    private final int code;
    private final String message;
}
