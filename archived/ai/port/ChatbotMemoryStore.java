package com.rabbiter.em.ai.port;

import java.util.List;

/**
 * 会话记忆存储端口（Port）。
 * <p>
 * 抽象多轮对话历史的持久化。
 * 当前实现：基于 Redis（langchain4j-community-redis-spring-boot-starter 自动装配）
 * 未来可换：PostgreSQL / Cassandra / 内存（用于测试）
 */
public interface ChatbotMemoryStore {

    /**
     * 读取该会话的历史消息。
     */
    List<ChatMessage> getMessages(String memoryId);

    /**
     * 追加一条消息到会话。
     */
    void appendMessage(String memoryId, ChatMessage message);

    /**
     * 清空会话历史。
     */
    void clear(String memoryId);

    /**
     * 通用消息结构（与 LangChain4j 内部模型解耦）。
     */
    record ChatMessage(Role role, String content) {
        public enum Role { SYSTEM, USER, AI }
    }
}
