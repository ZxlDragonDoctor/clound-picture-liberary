package com.zxl.zxlpicturebackend.aop;

import com.zxl.zxlpicturebackend.Model.entity.User;
import com.zxl.zxlpicturebackend.Model.enums.UserRoleEnum;
import com.zxl.zxlpicturebackend.annotation.AuthCheck;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.service.UserService;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String musRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //不需要权限直接放行
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(musRole);
        if(mustRoleEnum==null){
            return joinPoint.proceed();
        }
        //当前用户没有权限抛出异常
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //当前用户权限不匹配要求的权限抛出异常
        if(!mustRoleEnum.getValue().equals(userRoleEnum.getValue())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //权限匹配放行
        return joinPoint.proceed();
    }
}
