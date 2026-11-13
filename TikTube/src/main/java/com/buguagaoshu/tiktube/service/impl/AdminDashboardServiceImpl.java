package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.dao.AdminDashboardDao;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.model.AdminDailyPoint;
import com.buguagaoshu.tiktube.model.AdminTotals;
import com.buguagaoshu.tiktube.model.DailyCountRow;
import com.buguagaoshu.tiktube.service.AdminDashboardService;
import com.buguagaoshu.tiktube.vo.AdminArticleStat;
import com.buguagaoshu.tiktube.vo.AdminDataSummary;
import com.buguagaoshu.tiktube.vo.AdminDistribution;
import com.buguagaoshu.tiktube.vo.AdminMetricBucket;
import com.buguagaoshu.tiktube.vo.AdminOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 管理后台全站数据统计
 *
 * 时间窗口统一按「本地自然日」切分：先把当前时间对齐到当地 00:00，
 * 再往前推 days - 1 天作为起点，保证「近 7 天」就是今天 + 前 6 天。
 * 与 CreatorStatsServiceImpl 同一套分桶算法，只是不带 user_id 边界。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000L;

    /**
     * 稿件榜的排序列白名单：前端传 key，这里映射成固定 SQL 片段，
     * 绝不把用户输入拼进 ORDER BY。
     */
    private static final Map<String, String> ORDER_SQL = Map.of(
            "periodView", "periodView DESC, viewCount DESC",
            "view", "a.view_count DESC",
            "like", "a.like_count DESC",
            "comment", "a.comment_count DESC",
            "createTime", "a.create_time DESC"
    );

    private static final String DEFAULT_ORDER_SQL = "periodView DESC, viewCount DESC";

    private final AdminDashboardDao adminDashboardDao;

    @Autowired
    public AdminDashboardServiceImpl(AdminDashboardDao adminDashboardDao) {
        this.adminDashboardDao = adminDashboardDao;
    }

    @Override
    public AdminOverview overview() {
        long now = System.currentTimeMillis();
        AdminTotals totals = adminDashboardDao.selectTotals(ArticleStatusEnum.NORMAL.getCode(), now);
        if (totals == null) {
            totals = new AdminTotals();
        }

        AdminOverview overview = new AdminOverview();
        overview.setUserCount(totals.getUserCount());
        overview.setArticleCount(totals.getArticleCount());
        overview.setViewCount(totals.getViewCount());
        overview.setLikeCount(totals.getLikeCount());
        overview.setCommentCount(totals.getCommentCount());
        overview.setFavoriteCount(totals.getFavoriteCount());
        overview.setDanmakuCount(totals.getDanmakuCount());
        overview.setPendingExamine(totals.getPendingExamine());
        overview.setPendingOpinion(totals.getPendingOpinion());
        overview.setBlockedUser(totals.getBlockedUser());
        overview.setGeneratedAt(now);

        // 近 7 日序列：顺手汇总出「近 7 日新增」，最后一天就是「今日新增」
        List<AdminDailyPoint> week = dailySeries(7, 0);
        AdminMetricBucket weekBucket = sum(week);
        overview.setWeekUser(weekBucket.getUser());
        overview.setWeekArticle(weekBucket.getArticle());
        overview.setWeekView(weekBucket.getView());
        overview.setWeekLike(weekBucket.getLike());
        overview.setWeekComment(weekBucket.getComment());
        overview.setWeekFavorite(weekBucket.getFavorite());
        overview.setWeekDanmaku(weekBucket.getDanmaku());

        if (!week.isEmpty()) {
            AdminDailyPoint today = week.get(week.size() - 1);
            overview.setTodayUser(today.getUser());
            overview.setTodayArticle(today.getArticle());
            overview.setTodayView(today.getView());
            overview.setTodayLike(today.getLike());
            overview.setTodayComment(today.getComment());
            overview.setTodayFavorite(today.getFavorite());
            overview.setTodayDanmaku(today.getDanmaku());
        }
        return overview;
    }

    @Override
    public AdminDataSummary trend(Integer days) {
        int normalized = AdminDashboardService.normalizeDays(days);
        long now = System.currentTimeMillis();

        AdminDataSummary summary = new AdminDataSummary();
        summary.setDays(normalized);
        summary.setStartTime(windowStart(now, normalized));
        summary.setEndTime(windowEnd(now));

        List<AdminDailyPoint> trend = dailySeries(normalized, 0);
        summary.setTrend(trend);
        summary.setTotals(sum(trend));

        // 上一周期：紧挨着当前窗口的等长区间
        summary.setPrevious(sum(dailySeries(normalized, normalized)));
        summary.setPeak(peak(trend));
        return summary;
    }

    @Override
    public AdminDistribution distribution(Integer days) {
        int normalized = AdminDashboardService.normalizeDays(days);
        long now = System.currentTimeMillis();
        long start = windowStart(now, normalized);
        long end = windowEnd(now);
        int status = ArticleStatusEnum.NORMAL.getCode();

        AdminDistribution distribution = new AdminDistribution();
        distribution.setCategories(adminDashboardDao.selectCategoryStats(start, end, status));
        distribution.setTypes(adminDashboardDao.selectTypeStats(status));
        return distribution;
    }

    @Override
    public List<AdminArticleStat> topArticles(Integer days, String sort, Integer limit) {
        int normalized = AdminDashboardService.normalizeDays(days);
        long now = System.currentTimeMillis();

        // 排序键不在白名单里就回落默认排序，非法值不报错也不拼进 SQL
        String orderSql = sort == null ? DEFAULT_ORDER_SQL : ORDER_SQL.getOrDefault(sort, DEFAULT_ORDER_SQL);

        return adminDashboardDao.selectTopArticles(
                windowStart(now, normalized),
                windowEnd(now),
                ArticleStatusEnum.NORMAL.getCode(),
                orderSql,
                AdminDashboardService.normalizeLimit(limit)
        );
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /**
     * 生成一段连续的每日序列，缺失天补 0
     *
     * @param days      天数
     * @param shiftDays 往回偏移多少个「整周期」，0 表示当前周期，days 表示上一个周期
     */
    private List<AdminDailyPoint> dailySeries(int days, int shiftDays) {
        long now = System.currentTimeMillis();
        long start = windowStart(now, days) - shiftDays * DAY_MILLIS;
        long end = start + days * DAY_MILLIS;

        List<DailyCountRow> rows = adminDashboardDao.selectDailyCounts(
                start,
                end,
                TimeZone.getDefault().getRawOffset(),
                DAY_MILLIS,
                ArticleStatusEnum.NORMAL.getCode()
        );

        List<AdminDailyPoint> series = new ArrayList<>(days);
        Map<Long, AdminDailyPoint> byDay = new HashMap<>();
        for (int i = 0; i < days; i++) {
            long date = start + (long) i * DAY_MILLIS;
            AdminDailyPoint point = new AdminDailyPoint();
            point.setDate(date);
            series.add(point);
            byDay.put(dayIndexOf(date), point);
        }
        if (rows != null) {
            for (DailyCountRow row : rows) {
                AdminDailyPoint point = byDay.get(row.getDayIndex());
                // 落在窗口外的脏数据（理论上不会有）直接丢弃
                if (point != null) {
                    point.add(row.getMetric(), row.getCnt());
                }
            }
        }
        return series;
    }

    private static AdminMetricBucket sum(List<AdminDailyPoint> series) {
        AdminMetricBucket bucket = new AdminMetricBucket();
        if (series == null) {
            return bucket;
        }
        for (AdminDailyPoint point : series) {
            bucket.setUser(bucket.getUser() + point.getUser());
            bucket.setArticle(bucket.getArticle() + point.getArticle());
            bucket.setView(bucket.getView() + point.getView());
            bucket.setLike(bucket.getLike() + point.getLike());
            bucket.setComment(bucket.getComment() + point.getComment());
            bucket.setFavorite(bucket.getFavorite() + point.getFavorite());
            bucket.setDanmaku(bucket.getDanmaku() + point.getDanmaku());
        }
        return bucket;
    }

    private static AdminDailyPoint peak(List<AdminDailyPoint> series) {
        AdminDailyPoint best = null;
        if (series == null) {
            return null;
        }
        for (AdminDailyPoint point : series) {
            if (best == null || point.getView() > best.getView()) {
                best = point;
            }
        }
        return best;
    }

    /** 时间窗口起点：todayStart - (days - 1) 天 */
    private static long windowStart(long now, int days) {
        return dayStart(now) - (long) (days - 1) * DAY_MILLIS;
    }

    /** 时间窗口终点（不含）：明天 00:00 */
    private static long windowEnd(long now) {
        return dayStart(now) + DAY_MILLIS;
    }

    /** 把时间戳对齐到当地 00:00 */
    private static long dayStart(long millis) {
        long offset = TimeZone.getDefault().getRawOffset();
        return ((millis + offset) / DAY_MILLIS) * DAY_MILLIS - offset;
    }

    /** 与 SQL 里 (时间 + tzOffset) DIV dayMillis 同一套算法 */
    private static long dayIndexOf(long millis) {
        return (millis + TimeZone.getDefault().getRawOffset()) / DAY_MILLIS;
    }
}