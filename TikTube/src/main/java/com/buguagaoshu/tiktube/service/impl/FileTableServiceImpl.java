package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.enums.FileStatusEnum;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.FileTableDao;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.service.FileTableService;


@Service("fileTableService")
public class FileTableServiceImpl extends ServiceImpl<FileTableDao, FileTableEntity> implements FileTableService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("upload_time");
        IPage<FileTableEntity> page = this.page(
                new Query<FileTableEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<FileTableEntity> findArticleVideo(long id) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", id);
        wrapper.eq("type", FileTypeEnum.VIDEO.getCode());
        wrapper.eq("status", FileStatusEnum.USED.getCode());
        return this.list(wrapper);
    }

    @Override
    public List<FileTableEntity> findArticle(long id) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", id);
        wrapper.eq("status", FileStatusEnum.USED.getCode());
        return this.list(wrapper);
    }

    @Override
    public FileTableEntity findFileByFilename(String fileName) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("file_new_name", fileName);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateFileStatus(long userId, long fileId, int fileType, String fileUrl) {
        FileTableEntity file = getById(fileId);
        if (file == null) {
            return  false;
        }
        if (file.getUploadUserId() != userId) {
            return false;
        }
        if (file.getType() != fileType) {
            return false;
        }
        if (!file.getFileUrl().equals(fileUrl)) {
            return false;
        }
        if (file.getStatus() == FileStatusEnum.USED.getCode()) {
            return true;
        }
        file.setStatus(FileStatusEnum.USED.getCode());
        updateById(file);
        return true;
    }

    @Override
    public List<FileTableEntity> deprecatedFileList(long endTime, int count) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        // 是临时文件
        wrapper.eq("status", FileStatusEnum.NOT_USE_FILE.getCode());
        // 结束时间
        wrapper.le("upload_time", endTime);
        // 暂时排除文章类型里的文件
        wrapper.ne("type", FileTypeEnum.ARTICLE.getCode());
        wrapper.last("limit " + count);
        return list(wrapper);
    }


}