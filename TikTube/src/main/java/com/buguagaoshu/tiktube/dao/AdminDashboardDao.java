package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.model.AdminTotals;
import com.buguagaoshu.tiktube.model.DailyCountRow;
import com.buguagaoshu.tiktube.vo.AdminArticleStat;
import com.buguagaoshu.tiktube.vo.AdminCategoryStat;
import com.buguagaoshu.tiktube.vo.AdminTypeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理后台全站数据统计
 *
 * 与 {@link CreatorStatsDao} 同一套按天分桶口径，但不带 user_id 边界，
 * 只能由 /api/admin/** 下的接口调用（AdminInterceptor 校验管理员身份）。
 * 全部 SQL 见 AdminDashboardDao.xml。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Mapper
public interface AdminDashboardDao {

    /**
     * 累计统计与待办计数
     *
     * @param status 稿件状态，传 {@code ArticleStatusEnum.NORMAL.getCode()}
     * @param now    当前时间戳，用于判断封禁是否仍在生效
     */
    AdminTotals selectTotals(@Param("status") int status, @Param("now") long now);

    /**
     * 按天聚合的七项全站新增（注册 / 投稿 / 观看 / 点赞 / 评论 / 收藏 / 弹幕）
     *
     * @param startTime 区间起点（含），应为当地 00:00
     * @param endTime   区间终点（不含）
     * @param tzOffset  本地时区相对 UTC 的偏移毫秒，用于把时间戳对齐到自然日
     * @param dayMillis 一天的毫秒数
     */
    List<DailyCountRow> selectDailyCounts(@Param("startTime") long startTime,
                                          @Param("endTime") long endTime,
                                          @Param("tzOffset") long tzOffset,
                                          @Param("dayMillis") long dayMillis,
                                          @Param("status") int status);

    /**
     * 全站分区分布
     */
    List<AdminCategoryStat> selectCategoryStats(@Param("startTime") long startTime,
                                                @Param("endTime") long endTime,
                                                @Param("status") int status);

    /**
     * 全站内容类型分布
     */
    List<AdminTypeStat> selectTypeStats(@Param("status") int status);

    /**
     * 全站热门稿件榜（Top N）
     *
     * @param orderSql 只能取服务层白名单里的常量，禁止拼接用户输入
     * @param limit    服务层已收敛到 [1, 50]
     */
    List<AdminArticleStat> selectTopArticles(@Param("startTime") long startTime,
                                             @Param("endTime") long endTime,
                                             @Param("status") int status,
                                             @Param("orderSql") String orderSql,
                                             @Param("limit") int limit);
}