package com.lilac.ai;

import com.lilac.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;


@InputGuardrails({SafeInputGuardrail.class})
public interface AiHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String message);

    //学习报告
    record Report(String name, List<String> suggestionList){};

    //RAG
    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    /**
     * 使用GraphRAG进行聊天的方法，支持历史感知。
     * @MemoryId 注解确保LangChain4j将当前会话的memoryId传递给RAG流程，
     * 从而HistoryAwareRetrievalAugmentor可以获取到聊天历史。
     * @param memoryId 当前会话的记忆ID。
     * @param message 用户当前的问题。
     * @return 模型的回答。
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    String chatWithGraphRag(@MemoryId int memoryId,@UserMessage String message);

    //流式输出+GraphRAG
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStreamWithGraphRag(@MemoryId int memoryId, @UserMessage String message);

}
