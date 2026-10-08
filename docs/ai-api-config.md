# ObjectVerse AI API 配置说明

## 默认兜底机制

ObjectVerse 支持 Mock AI 兜底。没有 API Key、网络异常、接口失败或响应 JSON 解析失败时，系统会自动降级到 `MockAiClient`，保证本地演示和课程验收时页面不会因为大模型接口失败而 500。

## 阿里云百炼 Qwen API

项目通过 OpenAI-compatible Chat Completions 格式调用阿里云百炼 Qwen API。推荐配置如下：

```yaml
ai:
  provider: qwen
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  model: qwen-plus
  api-key: ${DASHSCOPE_API_KEY:}
```

请求地址由系统自动拼接为：

```text
https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
```

## 本地临时测试

为了快速验证功能，可以临时把真实 API Key 直接写入 `application.yml`：

```yaml
ai:
  api-key: sk-xxxxxxxx
```

这只适合本地临时测试。最终提交前必须改回：

```yaml
ai:
  api-key: ${DASHSCOPE_API_KEY:}
```

不要把真实 API Key 提交到压缩包、公开仓库、课程归档材料或任何共享文档中。

## IDEA 中配置环境变量

在 IntelliJ IDEA 中打开运行配置：

1. 进入 `Run/Debug Configurations`。
2. 选择 ObjectVerse Spring Boot 启动配置。
3. 在 `Environment variables` 中添加：

```text
DASHSCOPE_API_KEY=你的真实 API Key
```

4. 重新启动 Spring Boot 应用。

## 安全要求

页面不得展示 API Key。控制台不得打印完整 API Key。`ConfigurableAiClient` 只从 `AiProperties.getApiKey()` 读取 Key，Key 为空或调用失败时自动 fallback 到 `MockAiClient`。

## 降级规则

以下情况会自动使用 `MockAiClient`：

1. `provider=mock`。
2. `api-key` 为空。
3. `base-url` 为空。
4. `model` 为空。
5. HTTP 状态码不是 2xx。
6. 请求超时、网络异常或接口不可达。
7. Qwen 响应无法解析出 `choices[0].message.content`。
8. AI 返回内容无法解析为 ObjectVerse 需要的结构化 JSON。
