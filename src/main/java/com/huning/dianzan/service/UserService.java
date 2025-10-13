package com.huning.dianzan.service;

import com.huning.dianzan.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author huning
* @description 针对表【user】的数据库操作Service
* @createDate 2025-10-11 11:14:48
*/
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
}
