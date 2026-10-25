package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ltdz.taskflow.auth.JwtPayload;
import org.ltdz.taskflow.auth.JwtService;
import org.ltdz.taskflow.mapper.UserMapper;
import org.ltdz.taskflow.model.po.UserPo;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthService} 单元测试（依赖用 Mockito 模拟）。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long USER_ID = 1145141919810L;
    private static final String RIGHT_PASSWORD = "123456";

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokenWhenPasswordMatches() {
        when(userMapper.selectByPassword(RIGHT_PASSWORD)).thenReturn(user());
        when(jwtService.createToken(any(JwtPayload.class))).thenReturn("mocked.jwt.token");

        String token = authService.login(RIGHT_PASSWORD);

        assertEquals("mocked.jwt.token", token);

        ArgumentCaptor<JwtPayload> captor = ArgumentCaptor.forClass(JwtPayload.class);
        verify(jwtService).createToken(captor.capture());
        assertEquals(USER_ID, captor.getValue().getUserId(), "token 中应带正确的用户 id");
    }

    @Test
    void login_shouldThrowWhenPasswordWrong() {
        when(userMapper.selectByPassword("wrong-password")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login("wrong-password"));

        assertEquals("访问密钥无效", ex.getMessage());
        verify(jwtService, never()).createToken(any());
    }

    @Test
    void login_shouldThrowWhenPasswordEmpty() {
        when(userMapper.selectByPassword("")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.login(""));
        verify(jwtService, never()).createToken(any());
    }

    private UserPo user() {
        UserPo user = new UserPo();
        user.setId(AuthServiceTest.USER_ID);
        user.setPassword(RIGHT_PASSWORD);
        return user;
    }
}
