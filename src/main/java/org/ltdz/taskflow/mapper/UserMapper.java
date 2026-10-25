package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.UserPo;

/**
 * 用户表数据访问。
 */
@Mapper
public interface UserMapper {

    /**
     * 按访问密钥查询用户。
     * <p>
     * 账号由站主统一配置、不允许自主注册，密码不会重复，因此可直接作为唯一匹配条件。
     *
     * @param password 访问密钥
     * @return 匹配的用户；无匹配时返回 null
     */
    UserPo selectByPassword(@Param("password") String password);
}
