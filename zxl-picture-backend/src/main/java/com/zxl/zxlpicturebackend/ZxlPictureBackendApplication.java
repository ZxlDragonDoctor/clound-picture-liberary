package com.zxl.zxlpicturebackend;


import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.apache.shardingsphere.transaction.annotation.ShardingSphereTransactionType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;


@MapperScan("com.zxl.zxlpicturebackend.mapper")
@EnableAsync // 开启异步处理
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class}) // 暂不使用分表功能
@EnableAspectJAutoProxy(exposeProxy = true) //开启代理
public class ZxlPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxlPictureBackendApplication.class, args);
    }

}
