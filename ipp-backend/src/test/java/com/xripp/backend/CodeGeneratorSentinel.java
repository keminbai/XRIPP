package com.xripp.backend;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * 哨兵代码生成器（最终稳定版）
 *
 * 目标：
 * 1. 只生成 SysUser / Suppliers
 * 2. 强制生成 entity / mapper / service / serviceImpl / xml
 * 3. 兼容 Spring Boot 3 + Java 17
 */
public class CodeGeneratorSentinel {

    // ================= 数据库配置 =================
    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=XRIPP_CORE;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "123456";

    public static void main(String[] args) {

        String projectPath = System.getProperty("user.dir");

        System.out.println("🚀 开始哨兵代码生成（SysUser / Suppliers）...");

        FastAutoGenerator.create(DB_URL, DB_USER, DB_PASS)

                // ========= 全局配置 =========
                .globalConfig(builder -> builder
                        .author("XRIPP Team")
                        // .enableSwagger()           // ← 注释掉或删除
                        .dateType(DateType.ONLY_DATE)
                        .outputDir(projectPath + "/src/main/java")
                )

                // ========= 包结构配置 =========
                .packageConfig(builder -> builder
                        .parent("com.xripp.backend")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper")
                        .pathInfo(Collections.singletonMap(
                                OutputFile.xml,
                                projectPath + "/src/main/resources/mapper"
                        ))
                )

                // ========= 生成策略 =========
                .strategyConfig(builder -> builder
                        .addInclude("sys_user", "suppliers") // 只生成这两张表

                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .naming(NamingStrategy.underline_to_camel)

                        .mapperBuilder()
                        .enableBaseResultMap()
                        .enableBaseColumnList()

                        .serviceBuilder()
                        .formatServiceFileName("I%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                )

                // ========= ⚠️ 关键修复点：强制模板 =========
                .templateConfig(builder -> builder
                        .entity("/templates/entity.java")
                        .mapper("/templates/mapper.java")
                        .service("/templates/service.java")
                        .serviceImpl("/templates/serviceImpl.java")
                        .xml("/templates/mapper.xml")
                )

                // ========= 模板引擎 =========
                .templateEngine(new FreemarkerTemplateEngine())

                // ========= 执行 =========
                .execute();

        System.out.println("✅ 哨兵生成完成，请检查 Java 与 mapper 目录");
    }
}