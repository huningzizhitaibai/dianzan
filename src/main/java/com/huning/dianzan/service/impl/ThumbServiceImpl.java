package com.huning.dianzan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huning.dianzan.model.dto.thumb.DoThumbRequest;
import com.huning.dianzan.model.entity.Blog;
import com.huning.dianzan.model.entity.Thumb;
import com.huning.dianzan.model.entity.User;
import com.huning.dianzan.service.BlogService;
import com.huning.dianzan.service.ThumbService;
import com.huning.dianzan.mapper.ThumbMapper;
import com.huning.dianzan.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author huning
* @description 针对表【thumb】的数据库操作Service实现
* @createDate 2025-10-11 11:14:41
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService{
    private final UserService userService;
    private final BlogService blogService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        synchronized (loginUser.getId().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                boolean exsits = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .exists();
                if (exsits) {
                    throw new RuntimeException("用户已点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();


                //Thumb表记录的是, 点赞的用户, 和对应的blogId, 确保点赞的唯一性
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);

                return update && this.save(thumb);
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request){
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Thumb thumb = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .one();
                if (thumb == null) {
                    throw new RuntimeException("用户未点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbcount = thumbcount - 1")
                        .update();
                return update && this.removeById(thumb);
            });
        }
    }

}




