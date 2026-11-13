package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 管理后台趋势图一个周期内七项指标的合计，当前周期与上一周期各一份，环比由前端计算
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminMetricBucket {

    private long user;

    private long article;

    private long view;

    private long like;

    private long comment;

    private long favorite;

    private long danmaku;
}