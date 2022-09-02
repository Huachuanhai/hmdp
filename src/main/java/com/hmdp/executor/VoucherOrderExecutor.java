package com.hmdp.executor;

import com.hmdp.entity.VoucherOrder;
import com.hmdp.service.IVoucherOrderService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;

/**
 * @BelongsProject: hmdp
 * @BelongsPackage: com.hmdp.executor
 * @Author: devtest4
 * @CreateTime: 2022-09-02  18:41
 * @Description: 优惠价订单执行器
 * @Version: 1.0
 */
@Component
public class VoucherOrderExecutor {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Async("voucherOrderAsync")
    public Future<String> createVoucherOrderAsync(VoucherOrder voucherOrder) {
        voucherOrderService.createVoucherOrder(voucherOrder);
        return new AsyncResult<String>("完成");
    }
}
