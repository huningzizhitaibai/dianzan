package com.huning.dianzan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huning.dianzan.Constant.UserConstant;
import com.huning.dianzan.model.entity.User;
import com.huning.dianzan.service.UserService;
import com.huning.dianzan.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author huning
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-10-11 11:14:48
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    }

}




