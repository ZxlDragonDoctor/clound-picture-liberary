package com.zxl.zxlpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zxl.zxlpicturebackend.Model.dto.spaceuser.SpaceUserAddRequest;
import com.zxl.zxlpicturebackend.Model.dto.spaceuser.SpaceUserQueryRequest;
import com.zxl.zxlpicturebackend.Model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.zxlpicturebackend.Model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 朱小龙
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-23 20:41:18
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加团队空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验参数
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 构造构造对象
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 封装单条数据
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 封装列表数据
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
