package com.zxl.zxlpicturebackend.controller;

import com.zxl.zxlpicturebackend.Model.constant.UserConstant;
import com.zxl.zxlpicturebackend.Model.dto.space.analyze.*;
import com.zxl.zxlpicturebackend.Model.entity.Space;
import com.zxl.zxlpicturebackend.Model.entity.User;
import com.zxl.zxlpicturebackend.Model.vo.space.analyze.*;
import com.zxl.zxlpicturebackend.annotation.AuthCheck;
import com.zxl.zxlpicturebackend.common.BaseResponse;
import com.zxl.zxlpicturebackend.common.ResultUtils;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.exception.ThrowUtils;
import com.zxl.zxlpicturebackend.service.SpaceAnalyzeService;
import com.zxl.zxlpicturebackend.service.SpaceService;
import com.zxl.zxlpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;
    @Resource
    private UserService userService;

    /**
     * 分析空间图片使用情况
     * @param spaceUsageAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
                                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }

    /**
     * 分析图片分类使用情况
     * @param spaceCategoryAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponse = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponse);
   }

    /**
     * 分析图片标签使用情况
     * @param spaceTagAnalyzeRequest
     * @param request
     * @return
     */

   @PostMapping("tags")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
                                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponses = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyzeResponses);
  }

    /**
     * 分析图片大小使用情况
     * @param spaceSizeAnalyzeRequest
     * @param request
     * @return
     */
  @PostMapping("size")
    public  BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
      List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
      return ResultUtils.success(spaceSizeAnalyze);
  }

    /**
     * 分析用户上传情况
     * @param spaceUserAnalyzeRequest
     * @param request
     * @return
     */
  @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
                                                                           HttpServletRequest request) {
      ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
      User loginUser = userService.getLoginUser(request);
      List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponses = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
      return ResultUtils.success(spaceUserAnalyzeResponses);
  }

    /**
     * 分析空间使用排名情况
     * @param spaceRankAnalyzeRequest
     * @param request
     * @return
     */
  @PostMapping("rank")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) //仅限管理员使用
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
                                                                           HttpServletRequest request) {
      ThrowUtils.throwIf(spaceAnalyzeService==null, ErrorCode.PARAMS_ERROR);
      User loginUser = userService.getLoginUser(request);
      List<Space> spaceRankAnalyzeResponses = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
      return ResultUtils.success(spaceRankAnalyzeResponses);
    }
}
