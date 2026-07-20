package com.rabbiter.em.ai.port;

/**
 * AI 工具端口（Port）。
 * <p>
 * 所有让 LLM 调用的业务能力（查商品、下单、查订单...）都应实现此接口
 * 并注册到 {@code ChatbotToolRegistry}。
 * <p>
 * 工具的具体执行委托给对应业务域的 Service，AI 域不直接持有数据库访问。
 */
public interface ChatbotTool {

    /** 工具唯一标识（用于 LLM function-calling 路由） */
    String name();

    /** 给 LLM 看的工具描述（要清晰说明何时使用） */
    String description();

    /** JSON Schema 形式的入参定义 */
    String parametersSchema();
}
