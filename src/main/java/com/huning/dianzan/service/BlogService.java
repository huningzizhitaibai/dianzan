package com.huning.dianzan.service;

import com.huning.dianzan.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huning.dianzan.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* @author huning
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-10-11 11:13:24
*/
public interface BlogService extends IService<Blog> {
    BlogVO getBlogVOById(long blogId, HttpServletRequest request);
    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);

}
