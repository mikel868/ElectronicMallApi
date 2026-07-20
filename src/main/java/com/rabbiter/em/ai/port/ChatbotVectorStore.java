package com.rabbiter.em.ai.port;

import java.util.List;

/**
 * 向量库端口（Port）。
 * <p>
 * 用于 RAG（检索增强）场景的语义检索。
 * 当前实现：{@code RedisVectorStoreAdapter}（基于 langchain4j-community-redis）
 * 未来可换：Milvus / PGVector / Elasticsearch KNN / Pinecone
 */
public interface ChatbotVectorStore {

    /**
     * 把一段文本写入向量库（内部完成切分 + 向量化 + 存储）。
     *
     * @param text       原始文本
     * @param metadata   附加元数据（如来源、章节）
     * @return 写入的 chunk 数量
     */
    int ingest(String text, java.util.Map<String, Object> metadata);

    /**
     * 语义检索相关片段。
     *
     * @param query       查询文本
     * @param maxResults  最大返回数
     * @param minScore    最低相似度阈值（0.0 ~ 1.0）
     * @return 命中的片段列表
     */
    List<String> search(String query, int maxResults, double minScore);

    /**
     * 清空向量库（谨慎使用）
     */
    void clear();
}
