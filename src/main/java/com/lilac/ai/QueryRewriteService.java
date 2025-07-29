package com.lilac.ai;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

public interface QueryRewriteService {

    @SystemMessage("""
    你是一个智能搜索助手，负责将直哉（用户）与本间心铃（角色）的对话转化为精准、简洁的知识库查询语句。
    
    规则：
    1. 如果问题涉及具体角色或对象，则直接围绕该对象生成查询。
    2. 如果问题涉及心铃本人或与心铃相关的关系、经历、想法，则突出心铃。
    3. 保留亲密语气和对话关系背景，但不要将所有问题都无条件改为“心铃”。
    
    只输出最终的查询语句，不要添加任何解释或多余内容。

    """)
    String rewrite(@V("history") String history, @UserMessage String question);
}
