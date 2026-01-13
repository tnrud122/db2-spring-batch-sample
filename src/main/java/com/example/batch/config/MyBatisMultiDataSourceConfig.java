package com.example.batch.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Multi-DataSource MyBatis configuration:
 * - source: reads from SRC_TXN
 * - target: writes to TGT_TXN / AGG_DAILY and also used as Spring Batch metadata datasource (@BatchDataSource)
 */
@Configuration
@MapperScan(
        basePackages = "com.example.batch.mapper.source",
        sqlSessionTemplateRef = "sourceSqlSessionTemplate"
)
@MapperScan(
        basePackages = "com.example.batch.mapper.target",
        sqlSessionTemplateRef = "targetSqlSessionTemplate"
)
public class MyBatisMultiDataSourceConfig {

    @Bean("sourceSqlSessionFactory")
    public SqlSessionFactory sourceSqlSessionFactory(@Qualifier("sourceDataSource") DataSource ds) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        factory.setTypeAliasesPackage("com.example.batch.domain");
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mybatis/source/*.xml")
        );
        return factory.getObject();
    }

    @Bean("targetSqlSessionFactory")
    public SqlSessionFactory targetSqlSessionFactory(@Qualifier("targetDataSource") DataSource ds) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        factory.setTypeAliasesPackage("com.example.batch.domain");
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mybatis/target/*.xml")
        );
        return factory.getObject();
    }

    @Bean("sourceSqlSessionTemplate")
    public SqlSessionTemplate sourceSqlSessionTemplate(@Qualifier("sourceSqlSessionFactory") SqlSessionFactory sf) {
        return new SqlSessionTemplate(sf);
    }

    @Bean("targetSqlSessionTemplate")
    public SqlSessionTemplate targetSqlSessionTemplate(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sf) {
        return new SqlSessionTemplate(sf);
    }
}
