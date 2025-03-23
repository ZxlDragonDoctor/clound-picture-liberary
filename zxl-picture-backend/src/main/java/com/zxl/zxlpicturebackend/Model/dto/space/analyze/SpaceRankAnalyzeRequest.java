package com.zxl.zxlpicturebackend.Model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10; //默认前十名

    private static final long serialVersionUID = 1L;
}
