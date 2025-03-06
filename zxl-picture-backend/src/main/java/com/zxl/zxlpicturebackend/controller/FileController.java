package com.zxl.zxlpicturebackend.controller;


import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.zxl.zxlpicturebackend.Model.constant.UserConstant;
import com.zxl.zxlpicturebackend.annotation.AuthCheck;
import com.zxl.zxlpicturebackend.common.BaseResponse;
import com.zxl.zxlpicturebackend.common.ResultUtils;
import com.zxl.zxlpicturebackend.exception.BusinessException;
import com.zxl.zxlpicturebackend.exception.ErrorCode;
import com.zxl.zxlpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    private  CosManager cosManager;

    /**
     * 测试上传文件
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile){
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s",filename);
        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            // 返回可访问的图片地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("file upload error ,filepath = "+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if(file!=null){
                boolean delete = file.delete();
                if(!delete){
                    log.error("file delete error,filepath = {}",filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载
     * @param filepath
     * @param response
     * @throws IOException
     */
    @GetMapping("/test/download")
    public void testDownLoadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }finally{
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
