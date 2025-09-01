package com.sky.config;

import com.sky.properties.QiNiuOssProperties;
import com.sky.utils.QiNiuOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建QiNiuOssUtil对象
 */
@Configuration
@Slf4j
public class QiNiuOssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QiNiuOssUtil qiNiuOssUtil(QiNiuOssProperties qiNiuOssProperties){
        log.info("开始上传QiNiuOssUtil对象,配置:{}",qiNiuOssProperties);

        return new QiNiuOssUtil(qiNiuOssProperties.getAccessKey(),
                qiNiuOssProperties.getSecretKey(),
                qiNiuOssProperties.getBucket(),
                qiNiuOssProperties.getUrl());
    }
}
