package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 用户信息返回。
 */
@Data
public class UserVo {

    private Long userId;

    private String userName;

    /** 用户名旁展示的单字母 */
    private String userTitle;

    /** 首页展示的一句话 */
    private String userIndexTxt;
}
