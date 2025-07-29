package com.lilac.ai.rag;

import com.lilac.ai.QueryRewriteService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Slf4j
public class GraphRagRetriever implements ContentRetriever {

    private final String endpoint;
    private final QueryRewriteService queryRewriteService;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public GraphRagRetriever(String endpoint, QueryRewriteService queryRewriteService) {
        this.endpoint = endpoint;
        this.queryRewriteService = queryRewriteService;
    }

    @Override
    public List<Content> retrieve(Query query) {

        String queryToExecute = query.text();


        String encodedQuery = URLEncoder.encode(queryToExecute, StandardCharsets.UTF_8);
        String url = endpoint + "?query=" + encodedQuery;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("GraphRAG请求失败，状态码: " + response.code());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            Map<String, Object> result = mapper.readValue(responseBody, Map.class);
            String context = (String) result.getOrDefault("response", "");
            log.info("GraphRAG返回内容长度: {}", context.length());

            return List.of(Content.from(context));
        } catch (IOException e) {
            log.error("GraphRAG检索失败", e);
            throw new RuntimeException("GraphRAG检索失败", e);
        }
    }
}

