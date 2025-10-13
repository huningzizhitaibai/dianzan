package com.huning.dianzan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huning.dianzan.model.entity.Blog;
import com.huning.dianzan.model.entity.Thumb;
import com.huning.dianzan.model.entity.User;
import com.huning.dianzan.model.vo.BlogVO;
import com.huning.dianzan.service.BlogService;
import com.huning.dianzan.mapper.BlogMapper;
import com.huning.dianzan.service.ThumbService;
import com.huning.dianzan.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
* @author huning
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-10-11 11:13:24
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);

        if(loginUser == null) {
            return blogVO;
        }

        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();
        blogVO.setHasThumb(thumb!=null);
        return blogVO;
    }
}




