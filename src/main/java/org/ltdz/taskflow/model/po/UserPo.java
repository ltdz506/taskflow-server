package org.ltdz.taskflow.model.po;

import lombok.Data;

/**
 * 用户表实体（user）。
 */
@Data
public class UserPo {

    /** 用户 id（后端生成） */
    private Long id;

    /** 用户名 */
    private String name;

    /** 单字母（头像） */
    private String title;

    /** 首页展示文本 */
    private String indexTxt;

    /** 访问密钥（仅用于登录校验，不返回前端） */
    private String password;
}
