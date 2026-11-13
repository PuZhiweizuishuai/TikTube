package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 全站分区分布：稿件数 + 区间观看人数 + 累计播放
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminCategoryStat {

    private int category;

    /** 分区名，LEFT JOIN category 而来，分区被删时为空串 */
    private String categoryName;

    private long articleCount;

    private long periodView;

    private long viewCount;
}