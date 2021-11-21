package com.imooc.miaosha.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.pool.DruidDataSource;

@RestController
public class TestDruid {
    @Autowired
    private DataSource dataSource;

    @RequestMapping("/dataSource")
    public String dataSource() {
        try {
            Connection conn = dataSource.getConnection();
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            System.out.println("dataSource = "+dataSource);
            System.out.println("maxActive=" + druidDataSource.getMaxActive());
            System.out.println("maxWait=" + druidDataSource.getMaxWait());
            System.out.println("getMinEvictableIdleTimeMillis=" + druidDataSource.getMinEvictableIdleTimeMillis());
            System.out.println("getTimeBetweenEvictionRunsMillis=" + druidDataSource.getTimeBetweenEvictionRunsMillis());
            // System.out.println("conn = "+conn);
            return "success";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "end.";
    }
}
