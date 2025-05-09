package com.buguagaoshu.tiktube.schedule;

import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-12
 * 定期计算热门内容
 */
@Component
@Slf4j
public class HotTasks {
    private final ArticleService articleService;

    @Autowired
    public HotTasks(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 一小时计算一次
     * */
    @Scheduled(fixedRate = 3600000)
    public void hotTask() {
        log.info("开始计算当前热门内容......");
        HotCache.hotList = articleService.hotView(WebConstant.HOT_NUM);
        log.info("热门内容计算完成。");
    }
}
