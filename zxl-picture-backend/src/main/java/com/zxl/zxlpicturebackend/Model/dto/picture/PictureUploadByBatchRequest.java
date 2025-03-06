package com.zxl.zxlpicturebackend.Model.dto.picture;

import lombok.Data;

/**
 * 抓取图片请求
 */
@Data
public class PictureUploadByBatchRequest {
    /**
     * 名称前缀
     */
    private String namePrefix;

    /**  
     * 搜索词  
     */  
    private String searchText;  
  
    /**  
     * 抓取数量  
     */  
    private Integer count = 10;  
}
