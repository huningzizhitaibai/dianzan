package com.huning.dianzan.service;

import com.huning.dianzan.model.dto.thumb.DoThumbRequest;
import com.huning.dianzan.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author huning
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-10-11 11:14:41
*/
public interface ThumbService extends IService<Thumb> {
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);
    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);
    Boolean hasThumb(Long blogId, Long userId);
}
