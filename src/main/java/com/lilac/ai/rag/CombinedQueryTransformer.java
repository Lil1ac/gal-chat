package com.lilac.ai.rag;

import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class CombinedQueryTransformer implements QueryTransformer {

    private final CompressingQueryTransformer compressing;
    private final ExpandingQueryTransformer expanding;

    public CombinedQueryTransformer(OpenAiChatModel model, int expandCount, PromptTemplate compressPrompt) {
        this.compressing = new CompressingQueryTransformer(model, compressPrompt);
        this.expanding = new ExpandingQueryTransformer(model, expandCount);
    }

    @Override
    public Collection<Query> transform(Query query) {
        log.info("原始query为：{}", query);
        Collection<Query> compressedQueries = compressing.transform(query);
        List<Query> expandedQueries = new ArrayList<>();
        log.info("压缩后的query为：{}", compressedQueries);
//        for (Query compressed : compressedQueries) {
//            expandedQueries.addAll(expanding.transform(compressed));
//        }
//        log.info("扩展后的query为：{}",  expandedQueries);
        return compressedQueries;
    }
}
