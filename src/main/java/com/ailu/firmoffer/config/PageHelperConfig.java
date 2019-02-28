package com.ailu.firmoffer.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/28 9:59
 */
@Configuration
public class PageHelperConfig {
    @Value("${pagehelper.helperDialect}")
    private String helperDialect;

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", helperDialect);
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

}
