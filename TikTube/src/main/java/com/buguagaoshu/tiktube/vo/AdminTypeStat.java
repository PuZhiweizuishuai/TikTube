package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 全站内容类型分布【0 视频，1 图片，2 文章】
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminTypeStat {

    private int type;

    private long articleCount;

    private long viewCount;
}