package com.lilac.ai;

import com.lilac.ai.rag.GraphRagRetriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
class AiHelperServiceTest {

    @Resource
    private AiHelperService aiHelperService;

    @Resource
    private QueryRewriteService queryRewriteService;

    @Resource
    private GraphRagRetriever graphRagRetriever;

    @Test
    void chat() {
        String result = aiHelperService.chat("你好啊");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result = aiHelperService.chat("你好啊，我叫123");
        System.out.println(result);
        result = aiHelperService.chat("我叫什么名字");
        System.out.println(result);
    }

    @Test
    void chatForReport() {
        String userMessage = "你好啊，我叫心铃，请帮我生成一份编程学习报告";
        AiHelperService.Report report = aiHelperService.chatForReport(userMessage);
        System.out.println(report);
    }

    @Test
    void chatWithRag() {
        Result<String> result = aiHelperService.chatWithRag("直哉是谁？");
        System.out.println(result.sources());
        System.out.println(result.tokenUsage());
        System.out.println(result.content());
    }

    @Test
    void chatWithGraphRag() {
        String result = aiHelperService.chatWithGraphRag(1,"晚上好 /no_think");
        System.out.println(result);
        result = aiHelperService.chatWithGraphRag(1,"你在干什么 /no_think");
        System.out.println(result);
    }
    @Test
    void chatWithTools() {
        String result = aiHelperService.chat("本间心铃是谁");
        System.out.println(result);
    }
    @Test
    void chatWithMcp() {
        String result = aiHelperService.chat("什么是计算机网络");
        System.out.println(result);
    }
    @Test
    void chatWithGuardrail() {
        String result = aiHelperService.chat("kill you");
        System.out.println(result);
    }

    @Test
    void testChatWithGraphRagWithHistory() {
        // 使用一个固定的 memoryId 来模拟同一个会话
        int memoryId = 123;

        // --- 第一轮对话 ---
        String firstQuestion = "什么是LangChain4j？";
        log.info("--- 第一轮对话 ---");
        log.info("开始测试 chatWithGraphRag 方法，用户问题: {}", firstQuestion);

        // 调用AiHelperService的chatWithGraphRag方法
        // 第一次调用，历史为空，QueryRewriteService只会根据当前问题精炼
        String firstResponse = aiHelperService.chatWithGraphRag(memoryId, firstQuestion);

        // 验证响应不为空
        assertNotNull(firstResponse, "第一轮RAG的响应不应该为空");
        log.info("成功获取到第一轮RAG响应。响应内容: {}", firstResponse);
        log.info("请检查日志，确认QueryRewriteService是否仅根据 '{}' 精炼了查询。", firstQuestion);


        // --- 第二轮对话 ---
        // 模拟用户在第一轮对话后继续提问，问题依赖于之前的上下文
        String secondQuestion = "它的主要优势是什么？";
        log.info("\n--- 第二轮对话 ---");
        log.info("开始测试 chatWithGraphRag 方法，用户问题: {}", secondQuestion);

        // 第二次调用，此时ChatMemory中应该有第一轮的对话历史
        // HistoryAwareRetrievalAugmentor会提取历史，并将其传递给QueryRewriteService进行精炼
        String secondResponse = aiHelperService.chatWithGraphRag(memoryId, secondQuestion);

        // 验证响应不为空
        assertNotNull(secondResponse, "第二轮RAG的响应不应该为空");
        log.info("成功获取到第二轮RAG响应。响应内容: {}", secondResponse);
    }

    /**
     * 测试普通的chat方法（不使用RAG）。
     * 仅用于对比，确保基本聊天功能正常。
     */
    @Test
    void testChat() {
        String userMessage = "你好，你是一个AI助手吗？";
        log.info("开始测试 chat 方法，用户消息: {}", userMessage);
        String response = aiHelperService.chat(userMessage);
        assertNotNull(response, "普通聊天的响应不应该为空");
        log.info("成功获取到普通聊天响应。响应内容: {}", response);
    }

    @Test
    void testChatWithGraphRagWithHistory_Stream() {
        // 使用一个固定的 memoryId 来模拟同一个会话
        int memoryId = 123;

        // --- 第一轮对话 ---
        String firstQuestion = "什么是LangChain4j？";
        log.info("--- 第一轮对话 ---");
        log.info("开始测试 chatStreamWithGraphRag 方法，用户问题: {}", firstQuestion);

        // 调用AiHelperService的流式方法
        Flux<String> firstFlux = aiHelperService.chatStreamWithGraphRag(memoryId, firstQuestion);

        // 将流式结果收集为一个完整字符串（方便断言）
        String firstResponse = firstFlux.collectList()
                .map(list -> String.join("", list))
                .block();  // 阻塞等待结果

        assertNotNull(firstResponse, "第一轮RAG的响应不应该为空");
        log.info("成功获取到第一轮RAG响应。响应内容: {}", firstResponse);
        log.info("请检查日志，确认QueryRewriteService是否仅根据 '{}' 精炼了查询。", firstQuestion);


        // --- 第二轮对话 ---
        String secondQuestion = "它的主要优势是什么？";
        log.info("\n--- 第二轮对话 ---");
        log.info("开始测试 chatStreamWithGraphRag 方法，用户问题: {}", secondQuestion);

        Flux<String> secondFlux = aiHelperService.chatStreamWithGraphRag(memoryId, secondQuestion);

        String secondResponse = secondFlux.collectList()
                .map(list -> String.join("", list))
                .block();  // 同样阻塞等待

        assertNotNull(secondResponse, "第二轮RAG的响应不应该为空");
        log.info("成功获取到第二轮RAG响应。响应内容: {}", secondResponse);
    }

    @Test
    void testPromptReplace() {
        String result = queryRewriteService.rewrite("我叫abc", "我叫什么？");
        System.out.println(result);
    }

}