package com.lilac;

import com.lilac.ai.AiHelper;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiAgentApplicationTests {

    @Resource
    private AiHelper aiHelper;

    @Test
    void chat() {
        aiHelper.chat("你好!");
    }

    @Test
    void chatWithMessage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("描述图片"),
                ImageContent.from("https://web-demo-lilac.oss-cn-hangzhou.aliyuncs.com/06aa38b8-7f39-42b9-af55-8803b07b0bfc.png")
        );
        aiHelper.chatWithMessage(userMessage);
    }
}
