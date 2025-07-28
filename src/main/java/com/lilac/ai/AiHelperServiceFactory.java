package com.lilac.ai;

import com.lilac.ai.tools.MoeGirlSearchTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;
    @Bean
    public AiHelperService getAiHelperService() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        AiHelperService aiHelperService = AiServices.builder(AiHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel) // 流式输出
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
//                .contentRetriever(contentRetriever) // RAG
//                .tools(new MoeGirlSearchTool()) // 工具
//                .toolProvider(mcpToolProvider) // MCP
                .build();
        return aiHelperService;
    }
}
