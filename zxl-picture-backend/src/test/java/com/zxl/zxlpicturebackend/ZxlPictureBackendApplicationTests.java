package com.zxl.zxlpicturebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZxlPictureBackendApplicationTests {

    @Test
    void contextLoads() {
        String originUrl = "https://zxl-picture-1335108421.cos.ap-chongqing.myqcloud.com/public/2/2025-03-07_b0WT9cUoQPBBRmba.jpg";
        String keyOriginUrl  = originUrl.replace("https://zxl-picture-1335108421.cos.ap-chongqing.myqcloud.com/","");


        System.out.println(keyOriginUrl);
    }

}
