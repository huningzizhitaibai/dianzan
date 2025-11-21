package com.huning.dianzan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huning.dianzan.Constant.ThumbConstant;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * todo: Redis中的点赞记录设置过期时间, 实行冷热数据分离.
 * 用户点赞的博客发布时间大于1个月时, 判定为冷数据, 从Redis中删除
 */



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

                //直接查询redis中的数据
                boolean exists = this.hasThumb(blogId, loginUser.getId());

                if (exists) {
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

                boolean success = update && this.save(thumb);
                if (success) {
                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString(), thumb.getId());
                }
                return success;
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
                Object thumbIdObject = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                if(thumbIdObject == null){
                    throw new RuntimeException("用户未点赞");
                }
                Long thumbId = Long.valueOf(thumbIdObject.toString());


                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean success =  update && this.removeById(thumbId);
                if (success) {
                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                }
                return success;
            });
        }
    }

    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * 搜索Redis中是否存在对应key
     * 同一个userId对应多篇blogId
     * 其实, 这样来看, 在目前的业务中,其hashValue(thumbId)并没有什么作用
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
    }

}




