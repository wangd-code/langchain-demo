# LangChain4j Demo - 电力行业AI应用入门

> Phase 1 第一个项目：基于 Spring Boot 3 + LangChain4j 的电力行业 AI 对话应用

## 环境要求

| 项目 | 版本要求 | 检查命令 |
|------|---------|---------|
| JDK | 21+ (LTS) | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Ollama | 最新版 | `ollama --version` |

## 快速启动（3步）

### Step 1: 安装 JDK 21

下载地址（选一个）：
- [Eclipse Temurin JDK 21](https://adoptium.net/zh-CN/temurin/releases/?version=21&os=windows&arch=x64) （推荐）
- [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)

安装后设置环境变量：
```batch
set JAVA_HOME=D:/98depenApp/jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Step 2: 安装并启动 Ollama

1. 下载 Ollama: https://ollama.com/download
2. 安装后打开终端执行：
```batch
# 拉取通义千问7B模型（推荐，中文能力强）
ollama pull qwen2.5:7b

# 或者拉取其他模型
ollama pull llama3.1:8b
ollama pull glm4:9b

# 启动服务（通常安装后自动启动）
ollama serve
```

### Step 3: 运行项目

```batch
cd D:/workbuddyspace/langchain4j-demo

# 编译
mvn clean package -DskipTests

# 运行
java -jar target/langchain4j-demo-1.0.0-SNAPSHOT.jar
```

## 测试接口

### 简单对话
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"什么是台区线损？\"}"
```

### 流式对话
```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"解释一下无功补偿的原理\"}"
```

### 健康检查
```bash
curl http://localhost:8080/api/chat/health
```

## 切换 AI 模型

编辑 `src/main/resources/application.yml`，取消对应方案的注释：

| 方案 | 适用场景 | 费用 |
|------|---------|------|
| Ollama (默认) | 本地开发、离线环境 | 免费 |
| DashScope 通义千问 | 国内生产环境 | 按token计费 |
| OpenAI | 海外环境 | 按token计费 |

## 项目结构

```
langchain4j-demo/
├── pom.xml                              # Maven 依赖配置
├── src/main/java/com/power/ai/
│   ├── LangChain4jDemoApplication.java   # 启动类
│   ├── config/
│   │   └── AiModelConfig.java           # AI 模型配置（手动装配，学习用）
│   ├── controller/
│   │   └── ChatController.java          # REST 接口层
│   └── service/
│       └── ChatService.java            # 聊天服务（核心业务逻辑）
├── src/main/resources/
│   └── application.yml                  # 配置文件
└── src/test/java/com/power/ai/
    └── ChatServiceTest.java             # 集成测试
```

## 核心概念速查（Java开发者视角）

| LangChain4j 概念 | Java 类比 | 说明 |
|-----------------|-----------|------|
| ChatLanguageModel | Service 接口 | 同步调用大模型 |
| StreamingChatLanguageModel | 带回调的 Service | 流式调用大模型 |
| SystemMessage | 配置/上下文 | 设定AI角色和行为 |
| UserMessage | 方法入参 | 用户输入 |
| AiMessage | 方法返回值 | AI的回复 |
| ChatMemory | Session/会话 | 管理多轮对话上下文 |
| EmbeddingModel | 索引器 | 文本向量化（RAG核心） |
| Retrieval | 查询器 | 向量检索（RAG核心） |

## Phase 路线图

- [x] Phase 1.1: 项目搭建 + 第一个 AI 对话（同步 + 流式）
- [x] Phase 1.2: 多轮对话 + ChatMemory（手写实现）
- [x] Phase 1.3: StreamingChatModel 真正流式输出（逐 token 推送 SSE）
- [ ] Phase 2.1: Prompt 工程（CoT/Few-Shot/结构化输出）
- [ ] Phase 2.2: 接入 LangChain4j 官方 ChatMemory API
- [ ] Phase 2.3: 接入 EmbeddingModel + 向量存储（RAG 基础）

---

## 已完成功能速查

| 功能 | 文件 | 完成时间 |
|------|------|----------|
| Spring Boot 手动装配 AI 模型（不用 starter） | `AiModelConfig.java` | 06.06 |
| SystemMessage 角色设定（电力行业助手） | `ChatService.java` L46-57 | 06.06 |
| 同步对话接口 `POST /api/chat` | `ChatController.chat()` | 06.06 |
| 多轮对话 `POST /api/chat/multi`（手写 ChatMemory） | `ChatService.chatWithMemory()` | 06.12 |
| 清空/查看对话历史 | `ChatController.clearHistory()` / `getHistory()` | 06.12 |
| **真正流式输出 `POST /api/chat/stream`（逐 token 推送）** | `ChatService.streamChat()` + `ChatController.chatStream()` | **06.14** |

---

## 流式接口测试（真正逐 token 推送）

> 启动项目后，用 curl 测试（注意 `-N` 参数禁用缓冲，才能看到逐字效果）

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"用一句话解释什么是台区线损\"}"
```

**预期效果**：前端（或 curl）会收到多个 SSE event，每个 event 包含一个 token 片段，最终实现"打字机效果"。

**判断流式是否真正生效**：如果响应是一次收到完整句子 → 失败（假流式）；如果是一个字一个字收到 → 成功（真流式）。
