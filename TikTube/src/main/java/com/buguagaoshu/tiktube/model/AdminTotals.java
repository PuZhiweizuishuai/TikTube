package com.buguagaoshu.tiktube.model;

import lombok.Data;

/**
 * 管理后台概览的累计统计与待办计数，一条 SQL 的三个聚合派生表拼出
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminTotals {

    private long userCount;

    /** 当前封禁中的用户数（block_end_time 晚于当前时间） */
    private long blockedUser;

    private long articleCount;

    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    /** 待审核稿件数 */
    private long pendingExamine;

    /** 未处理举报 / 意见数 */
    private long pendingOpinion;
}