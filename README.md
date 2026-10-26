# Taskflow Server

「AI Agent 任务队列」个人网站的后端服务。

## 它在做什么

用户把要做的事拆成一条**任务流**（多个提示词按顺序排列），提交后由外部智能体（Agent）领取并逐步执行：

- **用户端**：创建/删除任务流、上传输入附件、查看执行进度与产物、修改设置
- **Agent 端**：定时轮询领取任务流、逐个取任务、上传产物、回复结果
- 状态变化通过 WebSocket 实时推给浏览器，前端不需要轮询

状态机：任务流 `编辑 → 启动 → 完成 / 销毁`；任务 `创建 → 等待 → 运行 → 终止`（单向）。

## 技术栈

Java 21 · Spring Boot 4.1 · MyBatis · MySQL 8 · JWT（jjwt）· 原生 WebSocket

## 本地运行

需要 jdk21+。

```bash
# 1. 打包，生成可执行 jar
./mvnw clean package -DskipTests

# 2. 用 java 命令启动
java -jar your-name.jar
```

