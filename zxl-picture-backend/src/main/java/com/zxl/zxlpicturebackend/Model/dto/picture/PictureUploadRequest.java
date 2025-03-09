package com.zxl.zxlpicturebackend.Model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;
    /**
     * 空间Id
     */
    private Long spaceId;

    /**
     * 图片地址
     */
    private String fileUrl;
    /**
     * 图片名称
     */
    private String picName;

    private static final long serialVersionUID = 1L;  
}
