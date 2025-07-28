package com.lilac.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 萌娘百科搜索工具
 */
@Slf4j
public class MoeGirlSearchTool {

    /**
     * 从萌娘百科搜索关键词相关的词条
     *
     * @param keyword 搜索关键词
     * @return 词条标题列表
     */
    @Tool(name = "moegirlSearch", value = """
            Retrieves relevant articles from zh.moegirl.org.cn based on a keyword.
            Use this tool when the user asks for anime, game, or moe-related topics.
            The input should be a clear search term.
            """
    )
    public String searchMoegirl(@P(value = "the keyword to search") String keyword) {
        List<String> results = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://zh.moegirl.org.cn/index.php?search=" + encodedKeyword;
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .get();
        } catch (IOException e) {
            log.error("get web error", e);
            return e.getMessage();
        }

        // 先尝试从条目页获取标题
        String pageTitle = doc.select("h1#firstHeading, h1.firstHeading").text();

        // 如果没有找到，再从 <title> 标签中解析
        if (pageTitle == null || pageTitle.isEmpty()) {
            String fullTitle = doc.title();
            if (fullTitle != null && !fullTitle.isEmpty()) {
                pageTitle = fullTitle.split(" - ")[0].trim();
            }
        }

        if (pageTitle != null && !pageTitle.isEmpty()) {
            // 说明是跳转到条目页面，直接返回条目标题和当前页面 URL
            results.add(pageTitle + " - " + doc.location());
        } else {
            // 取搜索结果列表
            Elements searchResults = doc.select(".mw-search-result-heading a");
            for (Element el : searchResults) {
                String title = el.text().trim();
                String link = "https://zh.moegirl.org.cn" + el.attr("href");
                results.add(title + " - " + link);
            }
        }

        if (results.isEmpty()) {
            return "没有找到相关条目";
        } else {
            return String.join("\n", results);
        }
    }
}
