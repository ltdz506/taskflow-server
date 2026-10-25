package org.ltdz.taskflow.service;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.JwtPayload;
import org.ltdz.taskflow.auth.JwtService;
import org.ltdz.taskflow.mapper.UserMapper;
import org.ltdz.taskflow.model.po.UserPo;
import org.springframework.stereotype.Service;

/**
 * 登录鉴权服务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtService jwtService;

    /**
     * 按访问密钥查询用户，查到即登录成功并签发 token。
     *
     * @param password 访问密钥
     * @return JWT 字符串
     * @throws IllegalArgumentException 密钥无效（无匹配用户）时抛出
     */
    public String login(String password) {
        UserPo user = userMapper.selectByPassword(password);
        if (user == null) {
            throw new IllegalArgumentException("访问密钥无效");
        }

        JwtPayload payload = new JwtPayload();
        payload.setUserId(user.getId());
        return jwtService.createToken(payload);
    }
}
