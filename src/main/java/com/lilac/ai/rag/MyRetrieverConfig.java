package com.lilac.ai.rag;

import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，注册 ContentRetriever 和 RetrievalAugmentor
 */
@Configuration
public class MyRetrieverConfig {

    @Resource
    private OpenAiChatModel openAiChatModel;

    PromptTemplate customCompressPrompt = PromptTemplate.from("""
            你是一个智能搜索助手。对话是直哉（用户）与本间心铃（角色）的交流。
            根据对话历史和直哉当前的问题，生成一句**精准、简洁**的知识库查询语句。
            生成规则：
            1. 如果问题与具体角色或对象相关，直接围绕该对象生成查询。
            2. 如果问题与心铃本人或心铃相关的关系、经历、想法有关，则重点突出心铃。
            3. 保留亲密语气和对话关系背景，但不要无条件将所有查询都改成“心铃”。
            历史会话: {{chatMemory}}
            直哉的问题: {{query}}
            只输出查询知识库语句，不要附加其他内容：
       """);

    /**
     * GraphRAG Retriever 实例
     */
    @Bean
    public ContentRetriever graphRagRetriever() {
        // 替换成你自己的实现，比如调用搜索服务
        return new GraphRagRetriever("http://localhost:8100/search/local");
    }

    /**
     * 合并查询转换器 + 默认增强器
     */
    @Bean
    public RetrievalAugmentor myRetrievalAugmentor() {
        QueryTransformer combinedTransformer = new CombinedQueryTransformer(
                openAiChatModel,
                1,
                customCompressPrompt
        );

        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(combinedTransformer)
                .contentRetriever(graphRagRetriever())
//                .contentInjector(DefaultContentInjector.builder()
//                        .promptTemplate(PromptTemplate.from("你现在是本间心铃。\n" +
//                                "直哉刚刚对你说：{{userMessage}}\n" +
//                                "结合以下知识库线索（可参考，可忽略）：{{contents}}\n" +
//                                "请你用心铃的口吻直接回应直哉，\n" +
//                                "专注当前对话情绪，不要总结关系或分析立场，不要旁白式表达。\n"))
//                        .build())
                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from("直哉说:{{userMessage}}\n\n知识库线索:{{contents}}"))
                        .build())
                .build();
    }
}
