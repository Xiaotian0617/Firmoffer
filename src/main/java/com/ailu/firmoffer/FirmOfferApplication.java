package com.ailu.firmoffer;

import com.github.pagehelper.PageInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.concurrent.*;

@Slf4j
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.ailu.firmoffer.dao.mapper")
public class FirmOfferApplication {

    public static ApplicationContext applicationContext;

    @Autowired
    private Environment env;

    @Autowired
    private PageInterceptor pageInterceptor;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(FirmOfferApplication.class, args);
    }

    @Bean
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("trading");
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);
        return taskExecutor;
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(dataSource);
        //该配置非常的重要，如果不将PageInterceptor设置到SqlSessionFactoryBean中，导致分页失效
        fb.setPlugins(new Interceptor[]{pageInterceptor});
        fb.setTypeAliasesPackage(env.getProperty("mybatis.type-aliases-package"));
        fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(env.getProperty("mybatis.mapper-locations")));
        return fb.getObject();
    }

    @Bean
    public Request.Builder getRequestBuilder() {
        return new Request.Builder();
    }

}
