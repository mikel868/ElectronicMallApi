package com.rabbiter.em.ai.port;

import reactor.core.publisher.Flux;

/**
 * LLM 客户端端口（Port）。
 * <p>
 * 业务代码只依赖此接口，不依赖任何具体厂商 SDK。
 * 切换 LLM 供应商（DashScope → DeepSeek → 自部署 Qwen → OpenAI）时，
 * 只需新增一个 Adapter 实现并切换 Spring 配置，业务逻辑零改动。
 */
public interface LlmClient {

    /**
     * 同步对话
     *
     * @param memoryId 会话 ID（多轮记忆隔离）
     * @param message  用户输入
     * @return LLM 完整回复
     */
    String chat(String memoryId, String message);

    /**
     * 流式对话（SSE 友好）
     *
     * @param memoryId 会话 ID
     * @param message  用户输入
     * @return token 流
     */
    Flux<String> streamChat(String memoryId, String message);
}
