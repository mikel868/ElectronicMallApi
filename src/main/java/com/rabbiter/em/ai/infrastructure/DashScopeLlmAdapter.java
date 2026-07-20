package com.rabbiter.em.ai.infrastructure;

import com.rabbiter.em.ai.port.LlmClient;
import com.rabbiter.em.ai.service.ConsultantService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * LlmClient 的 DashScope 适配器。
 * <p>
 * 委托给已有的 {@link ConsultantService}（langchain4j @AiService），
 * 在不破坏现有功能的前提下完成接入。
 * <p>
 * 未来切换 LLM 时：新增一个 {@code @Component} 实现 LlmClient 并加 {@code @Primary}，
 * 或用 {@code @ConditionalOnProperty} 切换。
 */
@Component
@ConditionalOnMissingBean(LlmClient.class)
public class DashScopeLlmAdapter implements LlmClient {

    private final ConsultantService delegate;

    public DashScopeLlmAdapter(ConsultantService delegate) {
        this.delegate = delegate;
    }

    @Override
    public String chat(String memoryId, String message) {
        // 当前 ConsultantService 只暴露流式接口，这里通过 blockFirst 桥接同步调用。
        // 真实业务推荐直接用 streamChat。
        return delegate.chat(memoryId, message).collectList().block().stream()
                .reduce("", String::concat);
    }

    @Override
    public Flux<String> streamChat(String memoryId, String message) {
        return delegate.chat(memoryId, message);
    }
}
