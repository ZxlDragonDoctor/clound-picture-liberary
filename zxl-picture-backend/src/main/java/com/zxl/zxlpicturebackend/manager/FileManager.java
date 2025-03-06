package com.zxl.zxlpicturebackend.manager;

import cn.hutool.Hutool;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.zxl.zxlpicturebackend.Model.dto.file.UploadPictureResult;
import com.zxl.zxlpicturebackend.common.ResultUtils;
import com.zxl.zxlpicturebackend.config.CosClientConfig;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.exception.ThrowUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件服务
 * @deprecated 已废弃，改为使用 upload 包的模板方法优化
 */
@Deprecated
@Service
@Slf4j
public class FileManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private CosManager cosManager;

    /**
     *
     * 上传图片
     * @param multipartFile 图片
     * @param uploadPathPrefix 上传的路径
     * @return 分装的基本图片信息对象
     */

        public UploadPictureResult  uploadPicture(MultipartFile multipartFile,String uploadPathPrefix){
            //校验图片
            validPicture(multipartFile);
            //拼接图片路径
            String uuid = RandomUtil.randomString(16);
            String originalFilename = multipartFile.getOriginalFilename();
            //拼接图片名称(日期+随机数+文件扩展名)
            String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,
                    FileUtil.getSuffix(originalFilename));
            //拼接图片上传路径
            String uploadPath =  String.format("/%s/%s",uploadPathPrefix,uploadFilename);
            //上传图片并获取图片基本信息
            File file = null;
            try {
                file = File.createTempFile(uploadPath, null);
                multipartFile.transferTo(file);
                PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
                //获取图片基本信息对象
                ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
                //计算宽高
                int picWidth = imageInfo.getWidth();
                int picHeight = imageInfo.getHeight();
                double picScale = NumberUtil.round(picWidth * 1.0 /picHeight,2).doubleValue();
                //封装图片基本信息对象
                UploadPictureResult uploadPictureResult = new UploadPictureResult();
                uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);  //返回域名加路径
                uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
                uploadPictureResult.setPicSize(FileUtil.size(file));
                uploadPictureResult.setPicWidth(picWidth);
                uploadPictureResult.setPicHeight(picHeight);
                uploadPictureResult.setPicScale(picScale);
                uploadPictureResult.setPicFormat(imageInfo.getFormat());

                //返回封装的对象
                return  uploadPictureResult;
            } catch (IOException e) {
                log.error("图片上传到对象存储失败 "+uploadPath,e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
            }finally {
                deleteTempFile(file);
            }
        }

    public UploadPictureResult  uploadPictureByUrl(String fileUrl,String uploadPathPrefix){
        //校验图片url格式是否正确
        validPicture(fileUrl);
        //拼接图片路径
        String uuid = RandomUtil.randomString(16);
//        String originalFilename = multipartFile.getOriginalFilename();
        String originalFilename = FileUtil.mainName(fileUrl);
        //拼接图片名称(日期+随机数+文件扩展名)
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,
                FileUtil.getSuffix(originalFilename));
        //拼接图片上传路径
        String uploadPath =  String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        //上传图片并获取图片基本信息
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
//            multipartFile.transferTo(file);
            HttpUtil.downloadFile(fileUrl,file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片基本信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //计算宽高
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 /picHeight,2).doubleValue();
            //封装图片基本信息对象
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);  //返回域名加路径
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回封装的对象
            return  uploadPictureResult;
        } catch (IOException e) {
            log.error("图片上传到对象存储失败 "+uploadPath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            deleteTempFile(file);
        }
    }

    /**
     * 删除临时文件
     * @param file
     */
    public void deleteTempFile(File file){
        if(file==null){
            return;
        }
        boolean delete = file.delete();
        if(!delete){
            log.error("file delete error,filepath = {}",file.getAbsoluteFile());
        }
    }

    /**
     * 校验本地上传图片格式
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        //1.校验图片大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
        //2.校验图片后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许图片上传的文件后缀列表
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg","png","jpeg","webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");
    }

    /**
     * 校验网路图片格式
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            /**
             * 发送head请求,获取图片元信息，例如文件类型，大小等
             */
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}
