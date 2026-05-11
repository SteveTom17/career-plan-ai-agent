package com.itheima.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
class CareerPlanAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;
    private final Resource resource;
    public CareerPlanAppDocumentLoader(ResourcePatternResolver resourcePatternResolver,
                                       @Value("classpath:/Your Repositories.html") Resource resource) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.resource = resource;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 这里可以修改为你要加载的多个 Markdown 文件的路径模式
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                String status = fileName.substring(fileName.length() - 6, fileName.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }

    /**
     * 自定义DocumentReader文档读取器，读取Github仓库信息
     * @return
     */
    public List<Document> loadGithubRepositoriesFromHtml() {
        List<Document> documents = new ArrayList<>();

        try {
            if (!this.resource.exists()) {
                log.error("HTML 文件不存在，请检查文件是否位于 src/main/resources/Your Repositories.html");
                return documents;
            }

            org.jsoup.nodes.Document html = Jsoup.parse(
                    this.resource.getInputStream(),
                    StandardCharsets.UTF_8.name(),
                    "https://github.com"
            );

            Elements repoItems = html.select("li[itemprop=owns]");

            // 兼容部分 GitHub 页面结构
            if (repoItems.isEmpty()) {
                repoItems = html.select("li.col-12.d-block.width-full.py-4.border-bottom");
            }

            log.info("共解析到 GitHub 仓库数量：{}", repoItems.size());

            for (Element repoItem : repoItems) {
                Element nameElement = repoItem.selectFirst("a[itemprop*=codeRepository]");

                if (nameElement == null) {
                    nameElement = repoItem.selectFirst("h3 a[href]");
                }

                if (nameElement == null) {
                    continue;
                }

                String repoName = nameElement.text()
                        .replaceAll("\\s+", " ")
                        .trim();

                String repoHref = nameElement.attr("href").trim();

                String repoUrl = repoHref.startsWith("http")
                        ? repoHref
                        : "https://github.com" + repoHref;

                Element descriptionElement = repoItem.selectFirst("p[itemprop=description]");

                String description = descriptionElement == null
                        ? "暂无描述"
                        : descriptionElement.text().replaceAll("\\s+", " ").trim();

                Element languageElement = repoItem.selectFirst("[itemprop=programmingLanguage]");

                String language = languageElement == null
                        ? "未知"
                        : languageElement.text().trim();

                Element updateElement = repoItem.selectFirst("relative-time");

                String updateTime = updateElement == null
                        ? "未知"
                        : updateElement.attr("datetime");

                Element starElement = repoItem.selectFirst("a[href$=/stargazers]");

                String stars = starElement == null
                        ? "0"
                        : starElement.text().replaceAll("\\s+", " ").trim();

                Element forkElement = repoItem.selectFirst("a[href$=/forks]");

                String forks = forkElement == null
                        ? "0"
                        : forkElement.text().replaceAll("\\s+", " ").trim();

                String content = """
                    GitHub 仓库信息：
                    仓库名称：%s
                    仓库地址：%s
                    仓库描述：%s
                    主要语言：%s
                    更新时间：%s
                    Stars：%s
                    Forks：%s
                    """.formatted(
                        repoName,
                        repoUrl,
                        description,
                        language,
                        updateTime,
                        stars,
                        forks
                );

                Document document = new Document(content, Map.of(
                        "repoName", repoName,
                        "repoUrl", repoUrl,
                        "description", description,
                        "language", language,
                        "updateTime", updateTime,
                        "stars", stars,
                        "forks", forks,
                        "source", "Your Repositories.html"
                ));

                documents.add(document);
            }

        } catch (IOException e) {
            log.error("GitHub 仓库 HTML 文档加载失败", e);
        }

        return documents;
    }
}