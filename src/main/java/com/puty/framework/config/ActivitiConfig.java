package com.puty.framework.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {

//    @Bean
//    @ConfigurationProperties("spring.datasource.druid.master")
//    public DruidDataSource activitiDataSource() {
//        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
//        return dataSource;
//    }

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(PlatformTransactionManager transactionManager,
                                                                             SpringAsyncExecutor springAsyncExecutor,
                                                                             DataSource dataSource) throws IOException {
        return baseSpringProcessEngineConfiguration(dataSource, transactionManager, springAsyncExecutor);
    }

}