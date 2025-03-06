package com.zxl.zxlpicturebackend.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zxl.zxlpicturebackend.Model.dto.user.UserQueryRequest;
import com.zxl.zxlpicturebackend.Model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.zxlpicturebackend.Model.vo.LoginUserVo;
import com.zxl.zxlpicturebackend.Model.vo.UserVO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 朱小龙
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-23 01:17:55
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     *数据脱敏
     * @param user
     * @return
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    boolean userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
