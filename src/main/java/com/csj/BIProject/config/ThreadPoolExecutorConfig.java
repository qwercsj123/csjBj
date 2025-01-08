package com.csj.BIProject.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {


    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){

        //初始化线程池  核心数为2  最大线程数为4  过期时间为100秒 工作队列使用ArrayBlockingQueue 当工作队列满了之后默认采用拒绝的策略
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8,16,100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100));


        return  threadPoolExecutor;
    }
}
