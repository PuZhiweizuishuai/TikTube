package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 管理后台概览：累计 KPI + 今日 / 近 7 日新增 + 待办计数
 *
 * 趋势序列不放在这里，由 /api/admin/dashboard/trend 按所选天数单独返回，
 * 避免概览与趋势图重复传输同一份数据。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminOverview {

    // ---- 累计 ----
    private long userCount;

    private long articleCount;

    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    // ---- 今日新增 ----
    private long todayUser;

    private long todayArticle;

    private long todayView;

    private long todayLike;

    private long todayComment;

    private long todayFavorite;

    private long todayDanmaku;

    // ---- 近 7 日新增 ----
    private long weekUser;

    private long weekArticle;

    private long weekView;

    private long weekLike;

    private long weekComment;

    private long weekFavorite;

    private long weekDanmaku;

    // ---- 待办 ----
    private long pendingExamine;

    private long pendingOpinion;

    private long blockedUser;

    /** 本次统计的生成时间，前端展示「最后更新」 */
    private long generatedAt;
}