package com.buguagaoshu.tiktube.vo;

import com.buguagaoshu.tiktube.model.AdminDailyPoint;
import lombok.Data;

import java.util.List;

/**
 * 管理后台趋势：区间合计 + 上一周期合计（算环比）+ 每日趋势 + 峰值日
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminDataSummary {

    private int days;

    private long startTime;

    private long endTime;

    private List<AdminDailyPoint> trend;

    private AdminMetricBucket totals;

    /** 紧挨当前窗口的等长上一周期合计 */
    private AdminMetricBucket previous;

    /** 区间内观看人数最高的一天 */
    private AdminDailyPoint peak;
}