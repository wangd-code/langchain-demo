package com.power.ai.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天服务 - 封装 LangChain4j 的核心 API
 *
 * 核心概念对照（Java开发者视角）：
 *
 * | LangChain4j 概念     | Java 类比            | 说明 |
 * |---------------------|---------------------|------|
 * | ChatLanguageModel   | Service接口          | 同步调用大模型 |
 * | StreamingChatModel  | 带回调的Service      | 流式调用大模型 |
 * | SystemMessage       | 配置/上下文           | 设定AI角色和行为 |
 * | UserMessage         | 方法入参             | 用户输入 |
 * | AiMessage           | 方法返回值           | AI的回复 |
 * | ChatMemory          | Session/会话         | 管理多轮对话上下文 |
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatModel;
    private final StreamingChatLanguageModel streamingChatModel;
    // 关键：用 ConcurrentHashMap 存储每个用户的对话历史
    private final java.util.Map<String, List<ChatMessage>> userConversations = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 电力行业系统提示词（SystemMessage示例）
     */
    private static final String POWER_INDUSTRY_SYSTEM_PROMPT = """
            你是一个电力行业AI助手，具备以下专业能力：
            1. 电力系统运行分析（负荷预测、潮流计算、故障诊断）
            2. 设备管理（变压器、开关柜、线路巡检）
            3. 安全规程解读（安规、两票三制）
            4. 数据分析（线损分析、电能质量、用电统计）
            
            回答要求：
            - 使用专业术语，但解释要通俗易懂
            - 涉及安全问题时，必须强调安全规程
            - 数据相关回答要注明数据来源和时效性
            """;

    /**
     * 简单对话（无上下文）
     *
     * @param userMessage 用户输入
     * @return AI回复
     */
    public String chat(String userMessage) {
        log.info("用户提问: {}", userMessage);

        List<ChatMessage> messages = List.of(
                SystemMessage.from(POWER_INDUSTRY_SYSTEM_PROMPT),
                UserMessage.from(userMessage)
        );

        ChatResponse response = chatModel.chat(messages);
        String answer = response.aiMessage().text();

        log.info("AI回复: {}", answer);
        return answer;
    }

    /**
     * 多轮对话核心方法
     * @param userId 用户ID（用来区分不同用户）
     * @param userMessage 用户当前消息
     * @return AI回复
     */
    public String chatWithMemory(String userId, String userMessage) {
        // 1. 获取或创建该用户的历史消息列表
        // 1. 获取或创建该用户的历史消息列表
        // computeIfAbsent: 如果 map 中不存在该 userId，则执行 lambda 表达式创建新列表并放入 map
        List<ChatMessage> messages = userConversations.computeIfAbsent(userId, k -> {
            List<ChatMessage> list = new ArrayList<>();
            // 首次对话，添加系统提示词作为上下文的基础
            list.add(SystemMessage.from(POWER_INDUSTRY_SYSTEM_PROMPT));
            return list;
        });

        // 2. 添加用户新消息
        messages.add(UserMessage.from(userMessage));

        // 3. 将所有历史消息传给 chat() 方法
        ChatResponse response = chatModel.chat(messages);

        // 4. 获取AI回复
        String aiReply = response.aiMessage().text();

        // 5. 将AI回复也加入历史
        messages.add(AiMessage.from(aiReply));

        // 6. 防止历史太长，限制最多保留最近20条消息
        if (messages.size() > 22) { // 1条系统提示 + 20条对话 + 1条缓冲
            // 保留系统提示词 + 最近20条消息
            List<ChatMessage> trimmed = new ArrayList<>();
            trimmed.add(messages.get(0)); // 系统提示词
            trimmed.addAll(messages.subList(messages.size() - 20, messages.size()));
            userConversations.put(userId, trimmed);
        }

        return aiReply;
    }

    /**
     * 清空某个用户的对话历史
     */
    public void clearHistory(String userId) {
        userConversations.remove(userId);
    }

    /**
     * 获取某个用户的对话历史（用于调试）
     */
    public List<ChatMessage> getHistory(String userId) {
        return userConversations.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * 流式对话（逐token返回，打字机效果）
     *
     * @param userMessage 用户输入
     * @return CompletableFuture，流式拼接完整回复
     */
    public CompletableFuture<String> chatStream(String userMessage) {
        log.info("流式提问: {}", userMessage);

        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder responseBuilder = new StringBuilder();

        List<ChatMessage> messages = List.of(
                SystemMessage.from(POWER_INDUSTRY_SYSTEM_PROMPT),
                UserMessage.from(userMessage)
        );

        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                responseBuilder.append(partialResponse);
                // TODO: Phase 2 通过 SSE 推送到前端
                log.debug("流式token: {}", partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                String fullResponse = responseBuilder.toString();
                log.info("流式回复完成: {}", fullResponse);
                future.complete(fullResponse);
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式回复异常", error);
                future.completeExceptionally(error);
            }
        });

        return future;
    }
}
