package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.dto.ExamineDto;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 视频，图片，文章 发帖表

TODO 回复消息可见，加密帖子，视频等
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface ArticleService extends IService<ArticleEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 发布视频
     * @return 发布结果
     * */
    ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request);


    ArticleViewData getVideo(long id, HttpServletRequest request);

    /**
     * 增加弹幕数
     * */
    void addDanmakuCount(Long id, long count);

    /**
     * 获取用户视频或其他列表
     * @param params 分页参数
     * @param id 用户ID
     * @param type 类型
     * @return 分页结果
     * */
    PageUtils userArticleList(Map<String, Object> params, Long id, Integer type);


    /**
     * 获取关注用户的稿件
     * */
    PageUtils fallowUserArticleList(Map<String, Object> params, Set<Long> userIds);


    /**
     * 获取用户上传的所有稿件
     * 仅登录后可访问
     * 只能查看自己的
     * */
    PageUtils userArticleList(Map<String, Object> params, String type, HttpServletRequest request);


    /**
     * 获取待审核视频
     * */
    PageUtils examineList(@RequestParam Map<String, Object> params, HttpServletRequest request);

    /**
     * 处理审核
     * */
    ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request);

    /**
     * 返回热门帖子
     * @param num 生成热点视频的数量
     * */
    List<ArticleViewData> hotView(int num);


    /**
     * 删除视频
     * @return  0 删除成功
     * 1 没权限
     * 2 不存在
     * */
    int deleteArticle(ArticleEntity entity, HttpServletRequest request);

    /**
     * 检查视频播放权限
     * @param file 视频文件
     * @param userId 用户信息和请求信息
     * @param request 获取浏览器 UA
     *                TODO 如果后期开放非登录用户也能播放，需要修改
     * */
    Boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request);

    /**
     * 返回当前分类下的视频
     * @param id 分类ID
     * @param params 查询参数
     * @return 视频列表
     * */
    PageUtils nowCategory(Map<String, Object> params, Integer id);


    ArticleViewData getEditInfo(Long id, HttpServletRequest request);


    /**
     * 更新视频
     * */
    ReturnCodeEnum updateVideo(VideoArticleDto videoArticleDto, HttpServletRequest request);


    /**
     * 恢复视频
     * */
    boolean restore(ArticleEntity articleEntity, HttpServletRequest request);



    void addCount(String col, Long articleId, long count);

    /**
     * 根据文章ID获取相似推荐内容
     * @param articleId 文章ID
     * @param limit 推荐数量限制
     * @return 推荐的文章列表
     */
    List<ArticleViewData> getRecommendationsByArticleId(Long articleId, int limit);
    
    /**
     * 根据标签列表获取推荐内容
     * @param tags 标签列表
     * @param excludeArticleId 需要排除的文章ID
     * @param limit 推荐数量限制
     * @return 推荐的文章列表
     */
    List<ArticleViewData> getRecommendationsByTags(List<String> tags, Long excludeArticleId, int limit);
}

