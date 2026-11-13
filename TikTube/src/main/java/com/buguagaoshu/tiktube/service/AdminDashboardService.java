package com.buguagaoshu.tiktube.service;

import com.buguagaoshu.tiktube.vo.AdminArticleStat;
import com.buguagaoshu.tiktube.vo.AdminDataSummary;
import com.buguagaoshu.tiktube.vo.AdminDistribution;
import com.buguagaoshu.tiktube.vo.AdminOverview;

import java.util.List;

/**
 * 管理后台全站数据统计，只允许管理员接口调用
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
public interface AdminDashboardService {

    /** 默认统计天数 */
    int DEFAULT_DAYS = 30;

    /** 最大统计天数，超过按它算，避免大区间把数据库拖垮 */
    int MAX_DAYS = 90;

    /** 热门稿件榜默认条数 */
    int DEFAULT_TOP_LIMIT = 10;

    /** 热门稿件榜最大条数 */
    int MAX_TOP_LIMIT = 50;

    /**
     * 概览：累计 KPI + 今日 / 近 7 日新增 + 待办计数
     */
    AdminOverview overview();

    /**
     * 趋势：区间合计 + 上一周期合计 + 每日趋势 + 峰值日
     *
     * @param days 统计天数，非法值回落到 {@link #DEFAULT_DAYS}
     */
    AdminDataSummary trend(Integer days);

    /**
     * 内容分布：分区维度 + 类型维度
     */
    AdminDistribution distribution(Integer days);

    /**
     * 热门稿件榜（Top N）
     *
     * @param sort  排序列键，只接受白名单内的值，其余回落到默认排序
     * @param limit 条数，收敛到 [1, {@link #MAX_TOP_LIMIT}]
     */
    List<AdminArticleStat> topArticles(Integer days, String sort, Integer limit);

    /**
     * 把前端传来的天数收敛到 [1, MAX_DAYS]
     */
    static int normalizeDays(Integer days) {
        if (days == null || days <= 0) {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }

    /**
     * 把前端传来的条数收敛到 [1, MAX_TOP_LIMIT]
     */
    static int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_TOP_LIMIT;
        }
        return Math.min(limit, MAX_TOP_LIMIT);
    }

    /** 稿件榜允许的排序列，前端只能传这里的 key */
    List<String> ALLOWED_SORTS = List.of("periodView", "view", "like", "comment", "createTime");
}