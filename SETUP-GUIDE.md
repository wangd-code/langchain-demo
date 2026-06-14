# LangChain4j 环境搭建指南

> 目标：今晚 19:30-21:00 用 90 分钟完成环境搭建，跑通第一个 AI 对话

## 前置检查

在 CMD 或 PowerShell 中运行以下命令，确认当前状态：

```batch
java -version
mvn -version
ollama --version
```

---

## 第一步：安装 JDK 21（约15分钟）

### 1.1 下载

浏览器打开以下任一地址（选 .msi 安装包，双击安装最省事）：

| 来源 | 下载地址 | 说明 |
|------|---------|------|
| **Eclipse Temurin（推荐）** | https://adoptium.net/zh-CN/temurin/releases/?version=21&os=windows&arch=x64 | 开源免费，社区活跃 |
| 清华镜像 | https://mirrors.tuna.tsinghua.edu.cn/Adoptium/ | 国内速度快 |
| 阿里镜像 | https://mirrors.aliyun.com/openjdk/ | 国内速度快 |

> 安装路径建议：`D:\98depenApp\jdk-21`（和你的 JDK 8/11 放一起）

### 1.2 配置环境变量

**方法A：修改系统环境变量（永久生效）**

1. Win + R → 输入 `sysdm.cpl` → 高级 → 环境变量
2. 新建/修改：
   - `JAVA_HOME` = `D:\98depenApp\jdk-21`
   - `Path` 中添加 `%JAVA_HOME%\bin`（移到最前面，覆盖 JDK 8）
3. 新开 CMD 确认：`java -version` 显示 21.x.x

**方法B：仅对当前终端生效（临时）**

```batch
set JAVA_HOME=D:\98depenApp\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
java -version
```

### 1.3 验证

```batch
java -version
# 应该显示：openjdk version "21.0.x" 或 java version "21"
```

---

## 第二步：修复 Maven 配置（约5分钟）

你的 Maven 在 `D:\98depenApp\apache-maven-3.9.6-bin\apache-maven-3.9.6\`

### 2.1 设置 JAVA_HOME

Maven 启动依赖 JAVA_HOME。在 CMD 中：

```batch
set JAVA_HOME=D:\98depenApp\jdk-21
set PATH=%JAVA_HOME%\bin;D:\98depenApp\apache-maven-3.9.6-bin\apache-maven-3.9.6\bin;%PATH%
mvn -version
```

应该显示：
```
Apache Maven 3.9.6
Java version: 21.x.x
```

### 2.2 配置国内 Maven 镜像（加速下载）

编辑 `D:\98depenApp\apache-maven-3.9.6-bin\apache-maven-3.9.6\conf\settings.xml`

在 `<mirrors>` 标签内添加：

```xml
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven Mirror</name>
    <url>https://maven.aliyun.com/repository/central</url>
</mirror>
```

---

## 第三步：安装 Ollama + 拉取模型（约15分钟）

### 3.1 安装 Ollama

1. 浏览器打开 https://ollama.com/download
2. 下载 Windows 版
3. 双击安装（一路 Next）

### 3.2 拉取模型

```batch
# 推荐第一个模型：通义千问 7B（中文最强，约4.7GB）
ollama pull qwen2.5:7b

# 可选：GLM4 9B（中文好，清华出品）
ollama pull glm4:9b

# 可选：Llama 3.1 8B（英文强）
ollama pull llama3.1:8b
```

> 模型下载可能需要 5-10 分钟，取决于网速

### 3.3 验证

```batch
# 测试 Ollama 是否正常
ollama run qwen2.5:7b
# 输入任意问题，如 "你好"，应该能看到回复
# 输入 /bye 退出
```

---

## 第四步：运行项目（约10分钟）

```batch
# 1. 设置环境（如果没做永久配置）
set JAVA_HOME=D:\98depenApp\jdk-21
set PATH=%JAVA_HOME%\bin;D:\98depenApp\apache-maven-3.9.6-bin\apache-maven-3.9.6\bin;%PATH%

# 2. 进入项目目录
cd D:\workbuddyspace\langchain4j-demo

# 3. 首次编译（会下载依赖，约3-5分钟）
mvn clean compile

# 4. 运行
mvn spring-boot:run
```

看到以下输出说明成功：
```
========================================
LangChain4j Demo 启动成功！
========================================
```

### 5. 测试接口

**新开一个 CMD 窗口：**

```batch
# 健康检查
curl http://localhost:8080/api/chat/health

# 简单对话
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d "{\"message\": \"什么是台区线损？\"}"

# 流式对话
curl -N -X POST http://localhost:8080/api/chat/stream -H "Content-Type: application/json" -d "{\"message\": \"解释无功补偿\"}"
```

---

## 如果用 IDEA 打开

1. File → Open → 选择 `D:\workbuddyspace\langchain4j-demo`
2. File → Project Structure → SDKs → 添加 JDK 21 路径
3. 等待 Maven 索引完成
4. 运行 `LangChain4jDemoApplication.main()`

---

## 常见问题

| 问题 | 解决方案 |
|------|---------|
| `mvn` 命令找不到 | 检查 PATH 是否包含 Maven 的 bin 目录 |
| `UnsupportedClassVersionError` | JAVA_HOME 指向了 JDK 8，需改为 JDK 21 |
| Ollama 连接失败 | 确认 `ollama serve` 已启动，浏览器访问 http://localhost:11434 |
| 模型回复乱码 | CMD 编码问题，用 `chcp 65001` 切换到 UTF-8 |
| 下载依赖慢 | 检查 Maven settings.xml 是否配置了阿里云镜像 |

---

## 时间规划

| 步骤 | 预计时间 | 累计 |
|------|---------|------|
| JDK 21 安装+配置 | 15分钟 | 15分钟 |
| Maven 修复+镜像配置 | 5分钟 | 20分钟 |
| Ollama 安装+拉模型 | 15分钟 | 35分钟 |
| 项目编译+运行 | 10分钟 | 45分钟 |
| 测试接口+调通 | 10分钟 | 55分钟 |
| **剩余时间** | **35分钟** | 用来看代码、理解核心概念 |

> 90分钟内完全可以搞定，还有余量回顾代码逻辑
