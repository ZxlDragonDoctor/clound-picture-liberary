package com.zxl.zxlpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zxl.zxlpicturebackend.Model.dto.space.analyze.*;
import com.zxl.zxlpicturebackend.Model.entity.Picture;
import com.zxl.zxlpicturebackend.Model.entity.Space;
import com.zxl.zxlpicturebackend.Model.entity.User;
import com.zxl.zxlpicturebackend.Model.vo.space.analyze.*;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.exception.ThrowUtils;
import com.zxl.zxlpicturebackend.service.PictureService;
import com.zxl.zxlpicturebackend.service.SpaceAnalyzeService;
import com.zxl.zxlpicturebackend.service.SpaceService;
import com.zxl.zxlpicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;


    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
        //查询所有图片或公共图库
        if(spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()){
            // 校验权限,只允许管理员访问
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest,longinUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            // 填充查询条件
            this.fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest,queryWrapper);
            // 查询数据库
            List<Object> list = pictureService.getBaseMapper().selectObjs(queryWrapper); //只查需要的字段
            long usedSize = list.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = list.size();
            //封装返回条件

            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);

        }else if(spaceUsageAnalyzeRequest.getSpaceId()!=null&&spaceUsageAnalyzeRequest.getSpaceId()>0){
            // 校验权限,只允许空间拥有者和管理员使用
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest,longinUser);
            // 查询数据库
            Space space = spaceService.getById(spaceUsageAnalyzeRequest.getSpaceId());
            //封装返回条件
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            // 计算百分比
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
        }
        return spaceUsageAnalyzeResponse;
    }

    /**
     * 分析空间分类情况
     * @param spaceCategoryAnalyzeRequest
     * @param longinUser
     * @return
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest==null,ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest,longinUser);
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        // 填充查询条件
        this.fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest,pictureQueryWrapper);
        // 使用mybatis-plus的分组查询
        pictureQueryWrapper.select("category,count(*) as count,sum(picSize) as totalSize").groupBy("category");
        return pictureService.getBaseMapper().selectMaps(pictureQueryWrapper)
                .stream().map(obj -> {
                    String category = (String)obj.get("category");
                    Long count = ((Number) obj.get("count")).longValue();
                    Long totalSize = ((Number) obj.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category,count,totalSize);
                }).toList();
    }

    /**
     * 分析空间标签情况
     * @param spaceTagAnalyzeRequest
     * @param longinUser
     * @return
     */

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest==null,ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest,longinUser);
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        //填充查询条件
        this.fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest,pictureQueryWrapper);
        //只查询tag字段
        pictureQueryWrapper.select("tags");
        List<String> tagsList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper)
                .stream()
                .filter(ObjectUtil::isNotNull) //过滤除非空标签
                .map(Objects::toString)
                .toList();
        //合并所有标签
        Map<String, Long> tagsCountMap = tagsList.stream()
                .flatMap(tags -> JSONUtil.toList(tags, String.class).stream()) //扁平化
                .collect(Collectors.groupingBy(tags -> tags, Collectors.counting()));//分组统计
        //构造返回对象并降序
        return tagsCountMap.entrySet().stream()
                .sorted((e1,e2)->Long.compare(e2.getValue(), e1.getValue())) //降序排列
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * 分析图片大小情况
     * @param spaceSizeAnalyzeRequest
     * @param longinUser
     * @return
     */

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest==null,ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest,longinUser);
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        //填充查询条件
        this.fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest,pictureQueryWrapper);
        pictureQueryWrapper.select("picSize");
        List<Long> picSizes = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper)
                .stream().map(obj -> (Long) obj).toList();
        //定义图片范围
        // 定义分段范围，注意使用有序 Map    ===>(LinkedHashNMap,TreeHashMap)
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizes.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizes.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizes.stream().filter(size -> size >= 1 * 1024 * 1024).count());
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 分析用户上传情况
     * @param spaceUserAnalyzeRequest
     * @param longinUser
     * @return
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest==null,ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest,longinUser);
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        //填充查询条件
        this.fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest,pictureQueryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        pictureQueryWrapper.eq(ObjectUtil.isNotNull(userId),"userId",userId);

        // 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                pictureQueryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                pictureQueryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            case "month":
                pictureQueryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        //分组和排序
        pictureQueryWrapper.groupBy("period").orderByAsc("period");
        List<Map<String, Object>> maps = pictureService.getBaseMapper().selectMaps(pictureQueryWrapper);
        return maps.stream()
                .map(map->{
                    String period =  map.get("period").toString();
                    Long count = (Long) map.get("count");
                    return new SpaceUserAnalyzeResponse(period,count);
                }).toList();
    }

    /**
     * 分析空间使用排行
     * @param spaceRankAnalyzeRequest
     * @param longinUser
     * @return
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User longinUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest==null,ErrorCode.PARAMS_ERROR);
        //校验权限
        ThrowUtils.throwIf(!userService.isAdmin(longinUser),ErrorCode.NO_AUTH_ERROR,"无权访问公共图库");

        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        //填充查询条件
        spaceQueryWrapper.select("id","spaceName","UserId","totalSize")
                .orderByAsc("totalSize")
                .last("LIMIT "+spaceRankAnalyzeRequest.getTopN()); //取前N名
        return spaceService.list(spaceQueryWrapper);
    }


    /**
     * 校验空间权限
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            // 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
        } else {
            // 私有空间权限校验
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 根据分析范围填充构造对象
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private  void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        //分析所有空间图片
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        // 分析公共图片
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        // 分析私有空间图片
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }



}
