package com.lilac.ai;

import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiHelperServiceTest {

    @Resource
    private AiHelperService aiHelperService;
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

}