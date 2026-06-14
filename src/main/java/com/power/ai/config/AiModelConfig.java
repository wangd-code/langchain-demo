package com.power.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 模型配置类
 *
 * 核心概念（Java开发者类比）：
 * - ChatLanguageModel ≈ Service接口，封装与大模型的同步交互
 * - StreamingChatLanguageModel ≈ 带回调的Service，流式返回结果
 * - OpenAiChatModel ≈ 具体实现类，对接OpenAI兼容接口（Ollama/DashScope等）
 *
 * 为什么手动配置而不是用Spring Boot Starter？
 * → Phase 1 先理解底层，Phase 2 再用 Starter 提效
 */
@Configuration
public class AiModelConfig {

    @Bean
    @ConfigurationProperties(prefix = "ai.model")
    public AiModelProperties aiModelProperties() {
        return new AiModelProperties();
    }

    /**
     * 同步聊天模型（一问一答，等完整回复）
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(AiModelProperties props) {
        return OpenAiChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .apiKey(props.getApiKey())
                .modelName(props.getModelName())
                .temperature(props.getTemperature())
                .maxTokens(props.getMaxTokens())
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * 流式聊天模型（逐token返回，打字机效果）
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(AiModelProperties props) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .apiKey(props.getApiKey())
                .modelName(props.getModelName())
                .temperature(props.getTemperature())
                .maxTokens(props.getMaxTokens())
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * AI 模型配置属性
     * 对应 application.yml 中的 ai.model 前缀
     */
    public static class AiModelProperties {
        private String baseUrl = "http://localhost:11434/v1";
        private String apiKey = "ollama";
        private String modelName = "qwen2.5:7b";
        private Double temperature = 0.7;
        private Integer maxTokens = 2048;

        // Getters and Setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    }
}
