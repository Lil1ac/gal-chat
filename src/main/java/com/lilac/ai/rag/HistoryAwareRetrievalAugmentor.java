package com.lilac.ai.rag;

import com.lilac.ai.QueryRewriteService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义检索增强器，用于实现历史感知的查询重写。
 * 它会在调用实际的内容检索器之前，先使用QueryRewriteService和聊天历史来精炼查询。
 */
@Slf4j
public class HistoryAwareRetrievalAugmentor implements RetrievalAugmentor {

    private final QueryRewriteService queryRewriteService;
    private final ContentRetriever underlyingContentRetriever;
    private final ChatMemoryProvider chatMemoryProvider;

    public HistoryAwareRetrievalAugmentor(QueryRewriteService queryRewriteService,
                                          ContentRetriever underlyingContentRetriever,
                                          ChatMemoryProvider chatMemoryProvider) {
        this.queryRewriteService = queryRewriteService;
        this.underlyingContentRetriever = underlyingContentRetriever;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {

        // Step 1: 获取metadata，里面包含当前问题，聊天ID，聊天历史
        Metadata metadata = augmentationRequest.metadata();
        UserMessage userChatMessage = (UserMessage) augmentationRequest.chatMessage();
        String currentQuestion = userChatMessage.singleText();
        Integer memoryId = (Integer) metadata.chatMemoryId();
        List<ChatMessage> historyMessages = metadata.chatMemory();

        // Step 2: 获取聊天历史，并将其拼接成一个字符串
        String history = "";
        if (memoryId != null && historyMessages.size() > 1) { // 如果大小为1，则说明是新会话，不需要历史
            // 提取聊天历史消息，将其拼接成一个字符串。
            history = historyMessages.stream()
                    .filter(msg -> {
                        // 排除当前这条用户消息
                        if (msg instanceof UserMessage) {
                            return !((UserMessage) msg).singleText().equals(currentQuestion);
                        }
                        // 跳过系统消息
                        return !(msg instanceof SystemMessage);
                    })
                    .map(msg -> {
                        if (msg instanceof AiMessage) {
                            return "[心铃] " + ((AiMessage) msg).text();
                        } else if (msg instanceof UserMessage) {
                            return "[直哉] " + ((UserMessage) msg).singleText();
                        } else {
                            return msg.toString();
                        }
                    })
                    .collect(Collectors.joining("\n"));

            log.info("提取到的聊天历史: {}", history);
        }

        // Step 3: 调用查询重写服务，精炼用户问题，传入历史和当前问题
        String rewrittenQuery = queryRewriteService.rewrite(history,currentQuestion);
        log.info("精炼后的查询语句 (包含历史): {}", rewrittenQuery);

        // Step 4: 使用精炼后的查询语句创建一个新的 Query 对象，并调用底层的 ContentRetriever
        Query rewrittenQueryObject = Query.from(rewrittenQuery, metadata);
        List<Content> retrievedContents = underlyingContentRetriever.retrieve(rewrittenQueryObject);
        log.info("所有检索到的内容:{}",retrievedContents);

        // Step 5: 注入内容
//        ChatMessage augmentedChatMessage = injectContentsIntoUserMessage(retrievedContents, userChatMessage);

        return AugmentationResult.builder()
                .chatMessage(userChatMessage) // 返回增强后的用户消息
                .contents(retrievedContents) // 返回所有检索到的内容
                .build();
    }

    /**
     * 注入逻辑（模拟 DefaultContentInjector.inject）
     */
    private ChatMessage injectContentsIntoUserMessage(List<Content> contents, UserMessage userMessage) {
        if (contents == null || contents.isEmpty()) {
            return userMessage;
        }

        // 将检索内容拼接成字符串
        StringBuilder knowledgeBuilder = new StringBuilder();
        for (Content content : contents) {
            knowledgeBuilder.append(content.textSegment().text()).append("\n");
        }

        // 注入模板，这里直接简单拼接
        String promptText = String.format(
                "直哉刚刚对你说：%s\n" +
                "结合以下知识库线索，用心铃自己的话回答（可参考，可忽略）：\n%s\n" +
                "请你用心铃的口吻直接回应直哉，专注当前对话情绪，不要总结关系或分析立场，不要旁白式表达。",
                userMessage.singleText(),
                knowledgeBuilder.toString()
        );

        // 保持用户 name（如果存在）
        if (userMessage.name() != null && !userMessage.name().isBlank()) {
            return UserMessage.from(promptText, userMessage.name());
        }
        return UserMessage.from(promptText);
    }
}
