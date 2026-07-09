package com.itheima.aiagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud-rag")
@Slf4j
class CareerPlanAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    /**
     * 将new对象的方式改为建造者模式
     * @return
     */
    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();
    }

    /**
     * 调用云知识库大模型
     * @return
     */
    @Bean
    public Advisor careerPlanAppRagCloudAdvisor() {
        DashScopeApi dashScopeApi = dashScopeApi();
        System.out.println("获取到的apikey为 :" +  dashScopeApi);
        final String KNOWLEDGE_INDEX = "career-plan-ai-agent";
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                 DashScopeDocumentRetrieverOptions.builder()
                     .withIndexName(KNOWLEDGE_INDEX)
                     .build());
        return RetrievalAugmentationAdvisor.builder()
        .documentRetriever(documentRetriever)
        .build();
    }
}
