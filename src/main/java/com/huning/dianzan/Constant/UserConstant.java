package com.huning.dianzan.Constant;

public interface UserConstant {
    /**
     * 用户登录态键
     * 标记的是在请求头中的键名
     */
    String USER_LOGIN_STATE = "user_login";

    // region 权限 从这开始是实质上的键值

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";



    //endregion
}
