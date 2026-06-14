package com.power.ai;

import com.power.ai.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChatService 集成测试
 *
 * 注意：运行测试需要 Ollama 服务已启动
 * 启动命令：ollama serve
 * 拉取模型：ollama pull qwen2.5:7b
 */
@SpringBootTest
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Test
    void testSimpleChat() {
        String answer = chatService.chat("什么是变压器？请用一句话回答");
        assertThat(answer).isNotBlank();
        System.out.println("AI回复: " + answer);
    }

    @Test
    void testPowerIndustryQuestion() {
        String answer = chatService.chat("电力系统中台区线损是什么意思？");
        assertThat(answer).isNotBlank();
        System.out.println("AI回复: " + answer);
    }
}
