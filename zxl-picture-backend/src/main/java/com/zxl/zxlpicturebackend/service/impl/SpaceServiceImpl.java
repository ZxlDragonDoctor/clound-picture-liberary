package com.zxl.zxlpicturebackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceAddRequest;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceEditRequest;
import com.zxl.zxlpicturebackend.Model.dto.space.SpaceQueryRequest;
import com.zxl.zxlpicturebackend.Model.entity.Space;
import com.zxl.zxlpicturebackend.Model.entity.User;
import com.zxl.zxlpicturebackend.Model.enums.SpaceLevelEnum;
import com.zxl.zxlpicturebackend.Model.enums.SpaceTypeEnum;
import com.zxl.zxlpicturebackend.Model.vo.SpaceVO;
import com.zxl.zxlpicturebackend.Model.vo.UserVO;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.exception.ThrowUtils;
import com.zxl.zxlpicturebackend.mapper.SpaceMapper;
import com.zxl.zxlpicturebackend.service.SpaceService;
import com.zxl.zxlpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author 朱小龙
 * @description 针对表【Space(空间)】的数据库操作Service实现
 * @createDate 2025-03-08 14:31:33
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private final Map<Long,Object> lockMap = new ConcurrentHashMap<>();


    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User longinUser) {
        //1.请求对象dto转换为实体类
        Space space = new Space();
        if(spaceAddRequest!=null){
            BeanUtil.copyProperties(spaceAddRequest,space);
        }
        // 2.填充默认值
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel()==null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填充空间限额
        this.fillSpaceBySpaceLevel(space);
        //3. 校验对象参数
        validSpace(space,true);
        //4. 创建空间
        Long userId = longinUser.getId();
        space.setUserId(userId);
        // 普通用户首次只能创建普通空间 ,管理员可以创建任意类型的空间 TODO:待加强逻辑
        if(SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()&& !userService.isAdmin(longinUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        //  针对用户加锁 （事物(transactionTemplate编程实现)加锁）
          //方法一：对字符串常量池（intern）进行加锁的
//        String lock = String.valueOf(userId).intern();
//        synchronized (lock){
//            Long newSpaceId = transactionTemplate.execute(status -> {
//                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId)
//                        .exists();
//                ThrowUtils.throwIf(exists,ErrorCode.OPERATION_ERROR,"每个用户只能有一个私有空间");
//                // 写入数据库
//                boolean result = this.save(space);
//                ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
//                return space.getId();
//            });
//            return Optional.ofNullable(newSpaceId).orElse(-1L);
//        }
        //方法二: 使用本地锁，采用 ConcurrentHashMap 来存储锁对象。
       Object lock =  lockMap.computeIfAbsent(userId,key->new Object());
        synchronized (lock){
            try{
                //判断是否已有私有空间和自己创建的团队空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType,space.getSpaceType())
                        .exists();
                ThrowUtils.throwIf(exists,ErrorCode.OPERATION_ERROR,"每个用户只能有一个私有空间或团队空间");
                // 写入数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
                return space.getId();
            }finally {
                // 防止内存泄露
                lockMap.remove(userId);
            }
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if(spaceType==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        // 修改数据时,如果要修改空间类型
        if (spaceLevel != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        //搜索
        queryWrapper.eq(ObjectUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(StrUtil.isNotBlank(spaceName),"spaceName",spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel),"spaceLevel",spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType),"spaceType",spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    @Override
    public void deleteSpace(long spaceId, User loginUser) {
        // 判断是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        this.checkSpaceAuth(loginUser,oldSpace);
       this.removeById(spaceId);
    }

    @Override
    public void editSpace(SpaceEditRequest spaceEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        this.validSpace(space,false);
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = this.getById(spaceEditRequest.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        this.checkSpaceAuth(loginUser,oldSpace);
        // 操作数据库
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = spaceVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 分页获取空间类封装
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> SpacePage, HttpServletRequest request) {
        List<Space> SpaceList = SpacePage.getRecords();
        Page<SpaceVO> SpaceVOPage = new Page<>(SpacePage.getCurrent(), SpacePage.getSize(), SpacePage.getTotal());
        if (CollUtil.isEmpty(SpaceList)) {
            return SpaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> SpaceVOList = SpaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = SpaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        SpaceVOList.forEach(SpaceVO -> {
            Long userId = SpaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            SpaceVO.setUser(userService.getUserVO(user));
        });
        SpaceVOPage.setRecords(SpaceVOList);
        return SpaceVOPage;
    }

    /**
     * 检查空间权限
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可操作
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
