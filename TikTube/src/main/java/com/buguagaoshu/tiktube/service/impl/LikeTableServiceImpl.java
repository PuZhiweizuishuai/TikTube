package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.LikeTableDao;
import com.buguagaoshu.tiktube.entity.LikeTableEntity;
import com.buguagaoshu.tiktube.service.LikeTableService;
import org.springframework.transaction.annotation.Transactional;


@Service("likeTableService")
public class LikeTableServiceImpl extends ServiceImpl<LikeTableDao, LikeTableEntity> implements LikeTableService {
    final ArticleService articleService;

    final CommentService commentService;

    final CountRecorder countRecorder;

    public LikeTableServiceImpl(ArticleService articleService,
                                CommentService commentService,
                                CountRecorder countRecorder) {
        this.articleService = articleService;
        this.commentService = commentService;
        this.countRecorder = countRecorder;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<LikeTableEntity> page = this.page(
                new Query<LikeTableEntity>().getPage(params),
                new QueryWrapper<LikeTableEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long likeObjId, Integer type, Long userId) {
        // 检查点赞目标是否存在
        Map<String, Object> map = new HashMap<>();
        boolean targetExists = false;
        if (type == 0) {
            // 检查文章是否存在且状态正常
            ArticleEntity article = articleService.getById(likeObjId);
            targetExists = article != null && article.getStatus() != 1;
        } else if (type == 1) {
            // 检查评论是否存在且状态正常
            CommentEntity comment = commentService.getById(likeObjId);
            targetExists = comment != null && comment.getStatus() != 1;
        } else {
            map.put("info", "点赞目标不存在");
            map.put("like", false);
            return map;
        }
        if (!targetExists) {
            map.put("info", "点赞目标不存在");
            map.put("like", false);
            return map;
        }
        // 检查用户是否已经点赞过

        LikeTableEntity existingLike = checkLike(likeObjId, type, userId);
        if (existingLike != null) {
            // 已点赞，执行取消点赞操作
            this.removeById(existingLike.getId());

            // 更新点赞计数
            if (type == 0) {
                countRecorder.recordArticleLike(likeObjId, -1);
                //articleService.addCount("like_count", likeObjId, -1);
            } else {
                countRecorder.recordCommentLike(likeObjId, -1);
                //commentService.addCount("like_count", likeObjId, -1);
            }

            map.put("info", "已经点过赞了");
        } else {
            // 未点赞，执行点赞操作
            LikeTableEntity likeTableEntity = new LikeTableEntity();
            likeTableEntity.setUserId(userId);
            likeTableEntity.setLikeObjId(likeObjId);
            likeTableEntity.setType(type);
            likeTableEntity.setCreateTime(System.currentTimeMillis());

            this.save(likeTableEntity);

            // 更新点赞计数
            if (type == 0) {
                countRecorder.recordArticleLike(likeObjId, 1);
                // articleService.addCount("like_count", likeObjId, 1L);
            } else {
                countRecorder.recordCommentLike(likeObjId, 1);
                //commentService.addCount("like_count", likeObjId, 1L);
            }

            map.put("info", "点赞成功！");
        }
        map.put("like", true);
        return map;
    }

    @Override
    public LikeTableEntity checkLike(Long likeObjId, Integer type, Long userId) {
        QueryWrapper<LikeTableEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("like_obj_id", likeObjId)
                .eq("type", type);

        return this.getOne(queryWrapper);
    }

}