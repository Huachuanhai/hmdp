package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注或取关
     * @param followUserId 关注用户id
     * @param isFollow 是否关注
     * @return Result
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 查询用户是否关注过
     * @param followUserId 关注用户id
     * @return Result
     */
    Result isFollow(Long followUserId);

    /**
     * 获取共同关注的博客
     * @param id 用户id
     * @return Result
     */
    Result followCommons(Long id);
}
