package com.lilac.ai.rag;

import com.lilac.ai.QueryRewriteService;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
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
                        return true;
                    })
                    .map(msg -> {
                        if (msg instanceof SystemMessage) {
                            return "[系统] " + ((SystemMessage) msg).text();
                        } else if (msg instanceof AiMessage) {
                            return "[AI] " + ((AiMessage) msg).text();
                        } else if (msg instanceof UserMessage) {
                            return "[用户] " + ((UserMessage) msg).singleText();
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
        // Query.from() 方法用于从文本和可选的元数据创建 Query 对象。
        Query rewrittenQueryObject = Query.from(rewrittenQuery, metadata);
        List<dev.langchain4j.rag.content.Content> retrievedContents = underlyingContentRetriever.retrieve(rewrittenQueryObject);

        // Step 5: 将检索到的内容包装成 AugmentationResult 返回
        return AugmentationResult.builder()
                .chatMessage(userChatMessage)
                .contents(retrievedContents)
                .build();
    }
}
