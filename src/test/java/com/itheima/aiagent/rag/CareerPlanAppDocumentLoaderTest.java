package com.itheima.aiagent.rag;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CareerPlanAppDocumentLoaderTest {

    @Resource
    private CareerPlanAppDocumentLoader careerPlanAppDocumentLoader;
    @Test
    void loadGithubRepositoriesFromHtml() {
        assertNotNull(careerPlanAppDocumentLoader, "careerPlanAppDocumentLoader 注入失败，请检查 @Component 和 @SpringBootTest");

        List<Document> documents = careerPlanAppDocumentLoader.loadGithubRepositoriesFromHtml();

        assertNotNull(documents, "返回的文档列表不能为 null");

        System.out.println("========== GitHub 仓库信息解析结果 ==========");
        System.out.println("共读取到仓库数量：" + documents.size());
        System.out.println();

        if (documents.isEmpty()) {
            System.out.println("未解析到 GitHub 仓库信息，请检查 HTML 文件内容或 Jsoup 选择器。");
            return;
        }

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            Map<String, Object> metadata = document.getMetadata();

            System.out.println("---------- 仓库 " + (i + 1) + " ----------");

            System.out.println("文档内容：");
            System.out.println(document.getText());

            System.out.println("元数据：");
            System.out.println("仓库名称：" + metadata.get("repoName"));
            System.out.println("仓库地址：" + metadata.get("repoUrl"));
            System.out.println("仓库描述：" + metadata.get("description"));
            System.out.println("主要语言：" + metadata.get("language"));
            System.out.println("更新时间：" + metadata.get("updateTime"));
            System.out.println("Stars：" + metadata.get("stars"));
            System.out.println("Forks：" + metadata.get("forks"));
            System.out.println("来源：" + metadata.get("source"));

            System.out.println();
        }
    }


}