-- 管理后台「数据概览」需要的索引（2026-11-13）
-- 已有库执行这一份即可，新库直接用 tik_tube.sql 建表已包含。
-- 全站按天聚合只按时间范围过滤，创作中心的 (对象, 时间) 索引前缀帮不上忙，
-- 需要补「时间在前」的索引，否则七路 UNION ALL 每一路都是全表扫描。
-- MySQL 的 DDL 不是幂等的，重复执行会报 Duplicate key name，先按下面的语句确认存在性：
--   SHOW INDEX FROM `user` WHERE Key_name = 'idx_user_create_time';

-- 每日新注册用户
ALTER TABLE `user`
    ADD INDEX `idx_user_create_time` (`create_time`) USING BTREE;

-- 每日新发布稿件（status = 0 为未删除）
ALTER TABLE `article`
    ADD INDEX `idx_article_status_time` (`status`, `create_time`) USING BTREE;

-- 每日观看人数：按更新时间范围扫描后回表 JOIN 稿件
ALTER TABLE `play_recording`
    ADD INDEX `idx_play_update_time` (`update_time`) USING BTREE;

-- 每日点赞（type = 0 为稿件，1 为评论）
ALTER TABLE `like_table`
    ADD INDEX `idx_like_type_time` (`type`, `create_time`) USING BTREE;

-- 每日评论（status = 0 为正常评论）
ALTER TABLE `comment`
    ADD INDEX `idx_comment_status_time` (`status`, `create_time`) USING BTREE;

-- 每日收藏
ALTER TABLE `favorites_table`
    ADD INDEX `idx_fav_create_time` (`create_time`) USING BTREE;

-- 每日弹幕（status = 0 为正常弹幕）
ALTER TABLE `danmaku`
    ADD INDEX `idx_danmaku_status_time` (`status`, `create_time`) USING BTREE;