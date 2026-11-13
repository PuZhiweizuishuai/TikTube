package com.buguagaoshu.tiktube.vo;

import lombok.Data;

import java.util.List;

/**
 * 内容分布：分区维度 + 类型维度，一次请求供两张图使用
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class AdminDistribution {

    private List<AdminCategoryStat> categories;

    private List<AdminTypeStat> types;
}