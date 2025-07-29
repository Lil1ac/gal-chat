package com.lilac.ai;

import com.lilac.ai.rag.GraphRagRetriever;
import com.lilac.ai.rag.HistoryAwareRetrievalAugmentor;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;

import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiHelperServiceFactory {

//    @Resource
//    private ChatModel myQwenChatModel;

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private OpenAiChatModel openAiLoraChatModel;

//    @Resource
//    private ContentRetriever contentRetriever;
//
//    @Resource
//    private McpToolProvider mcpToolProvider;

    @Resource
    private OpenAiStreamingChatModel OpenAiStreamingChatModel;




    /**
     * 配置并返回GraphRagRetriever实例。
     * 它现在只负责执行检索，不负责查询重写。
     * @return ContentRetriever实例
     */
    @Bean
    public ContentRetriever graphRagRetriever() {
        return new GraphRagRetriever("http://localhost:8100/search/local", queryRewriteService());
    }



    /**
     * 配置并返回QueryRewriteService实例。
     * 这个服务用于精炼用户问题。
     * @return QueryRewriteService实例
     */
    @Bean
    public QueryRewriteService queryRewriteService() {
        return AiServices.builder(QueryRewriteService.class)
                .chatModel(openAiChatModel)
                .build();
    }




    /**
     * 配置并返回ChatMemoryProvider实例。
     * 用于为每个会话提供独立的聊天记忆。
     * @return ChatMemoryProvider实例
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
    }




    /**
     * 配置并返回自定义的RetrievalAugmentor实例。
     * 这是RAG流程的核心，它会处理查询重写和内容检索。
     * @return RetrievalAugmentor实例
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor() {
        return new HistoryAwareRetrievalAugmentor(
                queryRewriteService(),          // 注入查询重写服务
                graphRagRetriever(),            // 注入底层的检索器
                chatMemoryProvider()            // 注入聊天记忆提供者
        );
    }



    /**
     * 配置并返回AiHelperService实例。
     * 现在它使用自定义的RetrievalAugmentor来处理RAG。
     * @return AiHelperService实例
     */
    @Bean
    public AiHelperService getAiHelperService() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        AiHelperService aiHelperService = AiServices.builder(AiHelperService.class)
                .chatModel( openAiLoraChatModel)
                .streamingChatModel(OpenAiStreamingChatModel) // 流式输出
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
//                .contentRetriever(contentRetriever) // RAG
//                .contentRetriever(graphRagRetriever()) // graphRAG
                .retrievalAugmentor(retrievalAugmentor()) // 启用自定义的RetrievalAugmentor来处理RAG
//                .tools(new MoeGirlSearchTool()) // 工具
//                .toolProvider(mcpToolProvider) // MCP
                .build();
        return aiHelperService;
    }
}
