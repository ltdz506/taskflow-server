package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ltdz.taskflow.mapper.UserMapper;
import org.ltdz.taskflow.model.po.UserPo;
import org.ltdz.taskflow.model.vo.UserVo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link UserService} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final long USER_ID = 1145141919810L;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserInfo_shouldMapAllFields() {
        UserPo user = new UserPo();
        user.setId(USER_ID);
        user.setName("蒋宏韬");
        user.setTitle("蒋");
        user.setIndexTxt("意亦时，吾已失\n疑久意久罢伊伶");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UserVo vo = userService.getUserInfo(USER_ID);

        assertEquals(USER_ID, vo.getUserId());
        assertEquals("蒋宏韬", vo.getUserName());
        assertEquals("蒋", vo.getUserTitle());
        assertEquals("意亦时，吾已失\n疑久意久罢伊伶", vo.getUserIndexTxt());
    }

    @Test
    void getUserInfo_shouldThrowWhenUserMissing() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.getUserInfo(1L));
    }
}
