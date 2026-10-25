package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.ltdz.taskflow.model.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link UserService} 集成测试：连真实数据库验证用户信息映射。
 */
@SpringBootTest
class UserServiceIntegrationTest {

    private static final long USER_ID = 1145141919810L;

    @Autowired
    private UserService userService;

    @Test
    void getUserInfo_withRealUser() {
        UserVo vo = userService.getUserInfo(USER_ID);

        assertEquals(USER_ID, vo.getUserId());
        assertEquals("蒋宏韬", vo.getUserName());
        assertEquals("蒋", vo.getUserTitle());
        assertNotNull(vo.getUserIndexTxt());

        System.out.println("[getUserInfo] userId       = " + vo.getUserId());
        System.out.println("[getUserInfo] userName     = " + vo.getUserName());
        System.out.println("[getUserInfo] userTitle    = " + vo.getUserTitle());
        System.out.println("[getUserInfo] userIndexTxt = " + vo.getUserIndexTxt());
    }
}
