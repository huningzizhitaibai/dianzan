package com.huning.dianzan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huning.dianzan.Constant.ThumbConstant;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据blogId 获取相关博客, 同时在返回时, 进行信息脱敏
     * @param blogId
     * @param request
     * @return
     */
    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    /**
     * 用于将blog信息进行脱敏
     * @param blog
     * @param loginUser
     * @return
     */
    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);

        if(loginUser == null) {
            return blogVO;
        }

        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogVO.setHasThumb(exist);
        return blogVO;
    }


    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjUtil.isNotEmpty(loginUser)) {
            //收集所有的查询到的blogId
            List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            // 获取点赞
            //从redis中获取用户对应所有的blogId的记录(当然, 没有点赞的记录是查不到的, 最终表中应该都是对应的bool)
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogIdList);
            for (int i =0 ;i<thumbList.size(); i++) {
                if (thumbList.get(i) == null)  {
                    continue;
                }
                blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
            }
        }

        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                })
                .toList();
    }

}




