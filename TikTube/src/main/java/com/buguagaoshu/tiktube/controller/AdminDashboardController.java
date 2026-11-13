package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.AdminDashboardService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台数据概览
 *
 * 全部接口落在 /api/admin/**，已被 AdminInterceptor 覆盖（校验 JWT 的 ADMIN 角色），
 * 因此这里不需要再判权限。全站聚合查询依赖
 * SQL_upgrade/upgrade-2026-11-13-admin-dashboard-index.sql 的时间索引。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Autowired
    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * 概览：累计 KPI + 今日 / 近 7 日新增 + 待办计数
     */
    @GetMapping("/overview")
    public ResponseDetails overview() {
        return ResponseDetails.ok().put("data", adminDashboardService.overview());
    }

    /**
     * 趋势：区间合计 + 上一周期合计（算环比）+ 每日趋势 + 峰值日
     *
     * @param days 统计天数，支持 7 / 30 / 90，其它值回落默认 30
     */
    @GetMapping("/trend")
    public ResponseDetails trend(@RequestParam(value = "days", required = false) Integer days) {
        return ResponseDetails.ok().put("data", adminDashboardService.trend(days));
    }

    /**
     * 内容分布：分区维度（稿件数 / 区间观看 / 累计播放）+ 类型维度
     */
    @GetMapping("/distribution")
    public ResponseDetails distribution(@RequestParam(value = "days", required = false) Integer days) {
        return ResponseDetails.ok().put("data", adminDashboardService.distribution(days));
    }

    /**
     * 热门稿件榜（Top N）
     *
     * @param sort  排序列，只接受 AdminDashboardService.ALLOWED_SORTS 里的值
     * @param limit 条数，默认 10，上限 50
     */
    @GetMapping("/top")
    public ResponseDetails top(@RequestParam(value = "days", required = false) Integer days,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseDetails.ok().put("data", adminDashboardService.topArticles(days, sort, limit));
    }
}