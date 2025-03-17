package com.zxl.zxlpicturebackend;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@MapperScan("com.zxl.zxlpicturebackend.mapper")
@EnableAsync // 开启异步处理
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true) //开启代理

public class ZxlPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxlPictureBackendApplication.class, args);
    }

}
