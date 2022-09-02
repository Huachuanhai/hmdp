package com.hmdp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @BelongsProject: hmdp
 * @BelongsPackage: com.hmdp.config
 * @Author: devtest4
 * @CreateTime: 2022-09-02  18:42
 * @Description: 线程池配置类
 * @Version: 1.0
 */
@Configuration
@EnableAsync
public class ExecutorConfiguration {

    private final int VOUCHER_ORDER_CORE_POOL_SIZE = 50;
    private final int VOUCHER_ORDER_Max_Pool_Size = 100;
    private final int VOUCHER_ORDER_QUEUE_CAPACITY = 1000000;

    @Bean
    public Executor voucherOrderAsync(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(VOUCHER_ORDER_CORE_POOL_SIZE);
        executor.setMaxPoolSize(VOUCHER_ORDER_Max_Pool_Size);
        executor.setQueueCapacity(VOUCHER_ORDER_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("hmdp-voucher-order-executor");
        executor.initialize();
        return executor;
    }
}
