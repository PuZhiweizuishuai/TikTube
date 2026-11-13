package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 全站热门稿件榜的一行，带作者名，口径与创作中心稿件榜一致（只统计未删除稿件）
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminArticleStat {

    private long id;

    private String title;

    private String imgUrl;

    private int type;

    private int category;

    private long userId;

    private String userName;

    private long createTime;

    private int examineStatus;

    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    /** 区间观看人数（去重） */
    private long periodView;

    private long periodLike;

    private long periodComment;
}