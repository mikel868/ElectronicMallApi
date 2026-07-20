package com.rabbiter.em.ai.infrastructure;

import com.rabbiter.em.ai.port.ChatbotVectorStore;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ChatbotVectorStore 的 Redis Stack 适配器。
 * <p>
 * 复用项目已经装配好的 RedisEmbeddingStore（来自 langchain4j-community-redis）。
 * 后续若数据量增大（百万级 chunk），新增 MilvusVectorStoreAdapter 并切换。
 */
@Component
@ConditionalOnMissingBean(ChatbotVectorStore.class)
public class RedisVectorStoreAdapter implements ChatbotVectorStore {

    private final RedisEmbeddingStore delegate;
    private final EmbeddingModel embeddingModel;

    public RedisVectorStoreAdapter(RedisEmbeddingStore delegate, EmbeddingModel embeddingModel) {
        this.delegate = delegate;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public int ingest(String text, Map<String, Object> metadata) {
        Document doc = Document.from(text, dev.langchain4j.data.document.Metadata.from(metadata));
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(delegate)
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(List.of(doc));
        // IngestionResult 在 1.0.1 不返回 segment 数量，这里返回 1 表示成功
        return 1;
    }

    @Override
    public List<String> search(String query, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches =
                delegate.search(EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(minScore)
                        .build()).matches();
        return matches.stream().map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .toList();
    }

    @Override
    public void clear() {
        // Redis 没有内置 clear，需运维通过 redis-cli FLUSHDB 处理对应库
        throw new UnsupportedOperationException(
                "Use redis-cli FLUSHDB on the embedding store DB instead.");
    }
}
