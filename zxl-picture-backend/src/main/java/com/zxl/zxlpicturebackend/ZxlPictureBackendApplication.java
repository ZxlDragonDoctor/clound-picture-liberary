package com.zxl.zxlpicturebackend;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.CrossOrigin;

@MapperScan("com.zxl.zxlpicturebackend.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true) //开启代理
public class ZxlPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxlPictureBackendApplication.class, args);
    }

}
