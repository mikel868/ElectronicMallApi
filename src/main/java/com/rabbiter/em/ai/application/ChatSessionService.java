package com.rabbiter.em.ai.application;

import com.rabbiter.em.ai.port.ChatbotMemoryStore;
import com.rabbiter.em.ai.port.ChatbotVectorStore;
import com.rabbiter.em.ai.port.LlmClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 客服会话编排服务（Application 层）。
 * <p>
 * 编排三个端口：{@link LlmClient} + {@link ChatbotMemoryStore} + {@link ChatbotVectorStore}。
 * Controller 只需要调用本类，不感知底层 LLM 厂商 / 向量库 / 存储实现。
 */
@Service
public class ChatSessionService {

    private final LlmClient llmClient;
    private final ChatbotMemoryStore memoryStore;
    private final ChatbotVectorStore vectorStore;

    public ChatSessionService(LlmClient llmClient,
                              ChatbotMemoryStore memoryStore,
                              ChatbotVectorStore vectorStore) {
        this.llmClient = llmClient;
        this.memoryStore = memoryStore;
        this.vectorStore = vectorStore;
    }

    /**
     * 流式对话（推荐入口，对应 SSE 接口）。
     */
    public Flux<String> streamChat(String memoryId, String message) {
        return llmClient.streamChat(memoryId, message);
    }

    /**
     * 同步对话（用于内部测试 / 非流式场景）。
     */
    public String chat(String memoryId, String message) {
        return llmClient.chat(memoryId, message);
    }

    /**
     * 重置会话记忆。
     */
    public void resetSession(String memoryId) {
        memoryStore.clear(memoryId);
    }

    /**
     * 注入新知识（运营上传新的客服文档时调用）。
     */
    public int ingestKnowledge(String text, java.util.Map<String, Object> metadata) {
        return vectorStore.ingest(text, metadata);
    }
}
