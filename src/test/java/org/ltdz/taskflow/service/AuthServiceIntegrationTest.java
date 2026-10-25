package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.ltdz.taskflow.auth.JwtPayload;
import org.ltdz.taskflow.auth.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AuthService} 集成测试：连真实数据库验证登录，并校验签发的 JWT。
 * <p>
 * 依赖 user 表中的唯一账号（密码 123456）。
 */
@SpringBootTest
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void login_withRealUser() {
        String token = authService.login("123456");

        assertNotNull(token);
        assertFalse(token.isBlank());

        JwtPayload payload = jwtService.parseToken(token);
        assertEquals(1145141919810L, payload.getUserId());
        assertNotNull(payload.getExpireAt());
        assertTrue(payload.getExpireAt().after(new Date()), "token 应在有效期内");

        System.out.println("[login] token    = " + token);
        System.out.println("[login] userId   = " + payload.getUserId());
        System.out.println("[login] expireAt = " + payload.getExpireAt());
    }
}
