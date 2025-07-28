package com.lilac.ai.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatModelListenerConfig {
    
    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                var request = requestContext.chatRequest();
                log.info("onRequest(): messages={}", request.messages()); // 或只取最后一条 request.messages().getLast()
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                var response = responseContext.chatResponse();
                log.info("onResponse(): output={}", response.toString()); // 只取返回文本
            }


            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }
}