package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * 根据id获取商户信息
     * @param id 商户id
     * @return Result
     */
    Result queryById(Long id);

    /**
     * 更新店铺数据
     * @param shop 参数
     * @return Result
     */
    Result updateShop(Shop shop);
}
