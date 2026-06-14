package com.power.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j AI应用入口
 *
 * 启动前提：
 * 1. JDK 21+ 已安装并配置 JAVA_HOME
 * 2. Ollama 已启动（或配置了其他 AI 模型 API Key）
 * 3. Maven 已配置
 */
@SpringBootApplication
public class LangChain4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangChain4jDemoApplication.class, args);
        System.out.println("""
            
            ========================================
            LangChain4j Demo 启动成功！
            接口文档：
              - POST http://localhost:8080/api/chat      简单对话
              - POST http://localhost:8080/api/chat/stream 流式对话
            ========================================
            """);
    }
}
