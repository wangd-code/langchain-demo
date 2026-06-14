package com.power.ai.controller;

import com.power.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天接口 - REST API 层
 *
 * 接口设计：
 * - POST /api/chat        → 简单同步对话
 * - POST /api/chat/stream  → SSE流式对话（打字机效果）
 *
 * 请求体示例：
 * {
 *   "message": "变压器常见的故障类型有哪些？"
 * }
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 简单对话接口
     *
     * 示例请求：
     * curl -X POST http://localhost:8080/api/chat \
     *   -H "Content-Type: application/json" \
     *   -d '{"message": "什么是台区线损？"}'
     */
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return Map.of("error", "message不能为空");
        }

        String answer = chatService.chat(message);
        return Map.of(
                "question", message,
                "answer", answer
        );
    }

    /**
     * 流式对话接口（SSE）
     *
     * 示例请求：
     * curl -N -X POST http://localhost:8080/api/chat/stream \
     *   -H "Content-Type: application/json" \
     *   -d '{"message": "解释一下电力系统中的无功补偿"}'
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        SseEmitter emitter = new SseEmitter(120_000L); // 2分钟超时

        if (message == null || message.isBlank()) {
            try {
                emitter.send(SseEmitter.event().data("{\"error\": \"message不能为空\"}"));
                emitter.complete();
            } catch (IOException e) {
                log.error("SSE发送错误", e);
            }
            return emitter;
        }

        executor.submit(() -> {
            try {
                CompletableFuture<String> future = chatService.chatStream(message);

                // 等待完整响应后一次性返回
                // TODO: Phase 2 改为逐token推送
                String response = future.get();
                emitter.send(SseEmitter.event().data(response));
                emitter.complete();
            } catch (Exception e) {
                log.error("流式对话异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 多轮对话接口
     * POST /api/chat/multi
     * Body: {"userId": "user123", "message": "什么是变压器？"}
     */
    @PostMapping("/multi")
    public Map<String, Object> chatWithMemory(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String message = request.get("message");

        if (userId == null || message == null) {
            return Map.of("error", "userId 和 message 不能为空");
        }

        String reply = chatService.chatWithMemory(userId, message);

        return Map.of(
                "userId", userId,
                "reply", reply,
                "historySize", chatService.getHistory(userId).size()
        );
    }

    /**
     * 清空对话历史
     * POST /api/chat/clear
     */
    @PostMapping("/clear")
    public Map<String, Object> clearHistory(@RequestParam String userId) {
        chatService.clearHistory(userId);
        return Map.of("message", "对话历史已清空", "userId", userId);
    }

    /**
     * 查看对话历史（调试用）
     * GET /api/chat/history?userId=user123
     */
    @GetMapping("/history")
    public Map<String, Object> getHistory(@RequestParam String userId) {
        return Map.of(
                "userId", userId,
                "history", chatService.getHistory(userId)
        );
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "LangChain4j Demo",
                "version", "1.0.0"
        );
    }
}
