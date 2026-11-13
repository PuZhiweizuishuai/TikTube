package com.buguagaoshu.tiktube.model;

import lombok.Data;

/**
 * 管理后台概览的「单日数据点」
 *
 * 与 {@link CreatorDailyPoint} 同一套按天分桶口径，但统计范围是全站：
 * user / article 是当日新注册数与新发布数，view 取 play_recording
 * （用户 × 分P 唯一一条，重复观看只更新 update_time），是「当日观看人数（去重）」，
 * 其余四项是事件表的真实新增量。缺失的天由服务层补 0。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminDailyPoint {

    /** 当天 00:00（本地时区）的时间戳 */
    private long date;

    /** 新注册用户数 */
    private long user;

    /** 新发布稿件数（未删除） */
    private long article;

    /** 观看人数（去重） */
    private long view;

    private long like;

    private long comment;

    private long favorite;

    private long danmaku;

    public void add(String metric, long count) {
        switch (metric) {
            case "user" -> this.user += count;
            case "article" -> this.article += count;
            case "view" -> this.view += count;
            case "like" -> this.like += count;
            case "comment" -> this.comment += count;
            case "favorite" -> this.favorite += count;
            case "danmaku" -> this.danmaku += count;
            default -> { /* 未知指标直接忽略，避免脏数据打断整个趋势 */ }
        }
    }
}