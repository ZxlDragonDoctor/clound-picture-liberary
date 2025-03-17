package com.zxl.zxlpicturebackend.manager;


import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.qcloud.cos.COSClient;

import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;

import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zxl.zxlpicturebackend.config.CosClientConfig;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;
    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }
    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象(附带图片信息)
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        //获取图片基本信息
        PicOperations picOperations = new PicOperations();
        //1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 图片压缩（转成webp格式）
        String webKey = FileUtil.mainName(key)+".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMor2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webKey);
        rules.add(compressRule);
        if(file.length() > 2 * 1024){
            // 缩略图处理
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 512, 512));
            rules.add(thumbnailRule);
        }
        //构造处理函数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
//    /**
//     * 获取图片主色调
//     *
//     * @param key 文件 key
//     * @return 图片主色调
//     */
//    public String getImageAve(String key) {
//        GetObjectRequest getObj = new GetObjectRequest(cosClientConfig.getBucket(), key);
//        String rule = "imageAve";
//        getObj.putCustomQueryParameter(rule, null);
//        COSObject object = cosClient.getObject(getObj);
//        COSObjectInputStream objectContent = object.getObjectContent();
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            CloseableHttpResponse httpResponse = httpClient.execute(objectContent.getHttpRequest());
//            String response = EntityUtils.toString(httpResponse.getEntity());
//            return JSONUtil.parseObj(response).getStr("RGB");
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }
//    }

    /**
     * 获取图片主色调（解决返回rgb不正常的bug）
     *
     * @param key 文件 key
     * @return 图片主色调
     */
    public String getImageAve(String key) {
        // 创建获取对象的请求
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        // 设置图片处理规则为获取主色调
        String rule = "imageAve";
        getObjectRequest.putCustomQueryParameter(rule,  null);
        // 获取对象
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        // 读取内容流并解析主色调信息
        try (COSObjectInputStream objectContent = cosObject.getObjectContent();
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            // 读取流内容
            byte[] buffer = new byte[1024];
            int length;
            while ((length = objectContent.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            // 将字节数组转换为字符串
            String aveColor = result.toString("UTF-8");
            return JSONUtil.parseObj(aveColor).getStr("RGB");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取图片主色调失败");
        }
    }
    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

}
