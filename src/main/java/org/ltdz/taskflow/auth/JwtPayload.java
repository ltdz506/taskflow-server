package org.ltdz.taskflow.auth;

import lombok.Data;

import java.util.Date;

/**
 * JWT 载荷：token 字符串与解析对象之间的数据载体。
 * <p>
 * 至少包含用户 id 和有效期截止时间；另带签发时间，便于排查问题。
 */
@Data
public class JwtPayload {

    /** 用户 id */
    private Long userId;

    /** 签发时间 */
    private Date issuedAt;

    /** 有效期截止时间 */
    private Date expireAt;
}
