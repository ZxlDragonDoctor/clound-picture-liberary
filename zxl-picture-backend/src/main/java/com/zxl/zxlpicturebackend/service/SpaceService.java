package com.zxl.zxlpicturebackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.zxlpicturebackend.Model.dto.picture.PictureUploadRequest;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceAddRequest;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceEditRequest;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceQueryRequest;
import com.zxl.zxlpicturebackend.Model.entity.Picture;
import com.zxl.zxlpicturebackend.Model.entity.Space;
import com.zxl.zxlpicturebackend.Model.entity.User;
import com.zxl.zxlpicturebackend.Model.vo.PictureVO;
import com.zxl.zxlpicturebackend.Model.vo.SpaceVO;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;

/**
* @author 朱小龙
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-08 14:37:02
*/
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param longinUser
     * @return
     */
    public long addSpace(SpaceAddRequest spaceAddRequest, User longinUser);

    /**
     * 校验空间请求
     *
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);

    /**
     * 自动填充空间限额即空间最大容量，最大容纳图片数
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    void deleteSpace(long spaceId, User loginUser);


    void editSpace(SpaceEditRequest spaceEditRequest, User loginUser);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> getSpaceVOPage(Page<Space> SpacePage, HttpServletRequest request);
}



