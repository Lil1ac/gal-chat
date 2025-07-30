package com.lilac.ai;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

public interface QueryRewriteService {

    @SystemMessage(fromResource = "retriever-prompt.txt")
    String rewrite(@V("history") String history, @UserMessage("question") String question);
}
