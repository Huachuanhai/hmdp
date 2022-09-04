package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 获取热度最高的博客
     * @param current 当前用户
     * @return Result
     */
    Result queryHotBlog(Integer current);

    /**
     * 根据id获取博客详情
     * @param id 伯格id
     * @return Result
     */
    Result queryBlogById(Long id);

    /**
     * 点赞
     * @param id blogId
     */
    Result likeBlog(Long id);

    /**
     * 获取Top5点赞过的用户
     * @param id 博客id
     * @return Result
     */
    Result queryBlogLikes(Long id);

    /**
     * 保存博客
     * @param blog 博客
     * @return Result
     */
    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);
}
