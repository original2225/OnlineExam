package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.mapper")
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);

        System.out.println("\n==============================================================");
        System.out.println("              在线考试管理系统  Online Exam System             ");
        System.out.println("==============================================================");
        System.out.println("  系统已成功启动！");
        System.out.println("  后端服务地址: http://localhost:9090");
        System.out.println("  请通过前端页面访问系统进行操作。");
        System.out.println("--------------------------------------------------------------");
        System.out.println("  功能模块包括：");
        System.out.println("   - 用户管理");
        System.out.println("   - 题库管理");
        System.out.println("   - 试卷管理");
        System.out.println("   - 在线考试");
        System.out.println("   - 成绩统计分析");
        System.out.println("--------------------------------------------------------------");
        System.out.println("  本系统仅供北冥服务器内部考核使用。");
        System.out.println("==============================================================\n");
    }
}