package org.ltdz.taskflow.service;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.mapper.UserMapper;
import org.ltdz.taskflow.model.po.UserPo;
import org.ltdz.taskflow.model.vo.UserVo;
import org.springframework.stereotype.Service;

/**
 * 用户信息服务。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 获取用户信息（不含密码）。
     *
     * @param userId 当前登录用户 id
     * @throws IllegalArgumentException 用户不存在时抛出
     */
    public UserVo getUserInfo(Long userId) {
        UserPo user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        UserVo vo = new UserVo();
        vo.setUserId(user.getId());
        vo.setUserName(user.getName());
        vo.setUserTitle(user.getTitle());
        vo.setUserIndexTxt(user.getIndexTxt());
        return vo;
    }
}
