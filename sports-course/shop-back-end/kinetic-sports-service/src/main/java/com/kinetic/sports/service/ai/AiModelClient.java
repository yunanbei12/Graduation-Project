package com.kinetic.sports.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinetic.sports.bean.dto.ai.AiCard;
import com.kinetic.sports.bean.model.AiKnowledge;
import com.kinetic.sports.service.config.AiModelProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelClient {

    private final AiModelProperties properties;
    private final ObjectMapper objectMapper;

    public String generateReply(String intent,
                                String userMessage,
                                String userProfileSummary,
                                List<AiKnowledge> knowledges,
                                List<AiCard> cards) {
        if (!properties.isEnabled()
                || !StringUtils.hasText(properties.getApiKey())
                || !StringUtils.hasText(properties.getBaseUrl())) {
            return null;
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", """
                            你是 KINETIC 体育教培课程系统的智能客服。
                            你的职责是用简洁、自然、专业的中文回答用户问题。
                            只基于给定的业务摘要、知识库和推荐卡片回答，不要编造不存在的订单、课包或优惠券。
                            如果用户问题需要人工跟进，可以在结尾建议联系人工客服。
                            回复不要使用 Markdown 标题或列表符号，保持自然对话风格。
                            """
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", buildPrompt(intent, userMessage, userProfileSummary, knowledges, cards)
            ));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", StringUtils.hasText(properties.getModelName()) ? properties.getModelName() : "gpt-4o-mini");
            body.put("temperature", resolveTemperature());
            body.put("messages", messages);

            String endpoint = resolveEndpoint(properties.getBaseUrl());
            String payload = objectMapper.writeValueAsString(body);
            int maxAttempts = Math.max(1, (properties.getRetryCount() == null ? 1 : properties.getRetryCount() + 1));
            Exception lastException = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(endpoint))
                            .timeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + properties.getApiKey())
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build();

                    HttpResponse<String> response = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(Math.min(resolveTimeoutSeconds(), 15)))
                            .build()
                            .send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        log.warn("AI 模型调用失败, provider={}, model={}, attempt={}, status={}, body={}",
                                properties.getProvider(), properties.getModelName(), attempt, response.statusCode(), response.body());
                        return null;
                    }

                    JsonNode root = objectMapper.readTree(response.body());
                    String content = root.path("choices").path(0).path("message").path("content").asText("");
                    return StringUtils.hasText(content) ? content.trim() : null;
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < maxAttempts) {
                        log.warn("AI 模型调用异常，准备重试, provider={}, model={}, attempt={}/{}, reason={}",
                                properties.getProvider(), properties.getModelName(), attempt, maxAttempts, e.getMessage());
                        continue;
                    }
                }
            }
            if (lastException != null) {
                log.warn("AI 模型调用异常, provider={}, model={}, timeout={}s, retries={}, reason={}",
                        properties.getProvider(), properties.getModelName(), resolveTimeoutSeconds(),
                        properties.getRetryCount(), lastException.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.warn("AI 模型请求构建异常, provider={}, model={}, reason={}",
                    properties.getProvider(), properties.getModelName(), e.getMessage());
            return null;
        }
    }

    private String resolveEndpoint(String baseUrl) {
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/")) {
            return normalized + "chat/completions";
        }
        return normalized + "/chat/completions";
    }

    private Object resolveTemperature() {
        String provider = properties.getProvider();
        String modelName = properties.getModelName();
        if (isKimiFixedTemperatureModel(provider, modelName)) {
            return 1;
        }
        return properties.getTemperature() != null ? properties.getTemperature() : 0.4D;
    }

    private boolean isKimiFixedTemperatureModel(String provider, String modelName) {
        if (!StringUtils.hasText(modelName)) {
            return false;
        }
        boolean isKimiProvider = "kimi".equalsIgnoreCase(provider)
                || (StringUtils.hasText(properties.getBaseUrl()) && properties.getBaseUrl().contains("moonshot.cn"));
        return isKimiProvider && modelName.toLowerCase().startsWith("kimi-k");
    }

    private int resolveTimeoutSeconds() {
        return properties.getTimeoutSeconds() != null && properties.getTimeoutSeconds() > 0
                ? properties.getTimeoutSeconds()
                : 60;
    }

    private String buildPrompt(String intent,
                               String userMessage,
                               String userProfileSummary,
                               List<AiKnowledge> knowledges,
                               List<AiCard> cards) {
        StringBuilder builder = new StringBuilder();
        builder.append("意图：").append(intent).append('\n');
        builder.append("用户问题：").append(userMessage).append('\n');
        builder.append("用户业务摘要：").append(userProfileSummary).append('\n');
        builder.append("命中的知识：").append('\n');
        if (knowledges == null || knowledges.isEmpty()) {
            builder.append("无").append('\n');
        } else {
            for (AiKnowledge knowledge : knowledges) {
                builder.append("- ").append(knowledge.getTitle()).append(": ").append(knowledge.getContent()).append('\n');
            }
        }
        builder.append("可展示卡片：").append('\n');
        if (cards == null || cards.isEmpty()) {
            builder.append("无").append('\n');
        } else {
            for (AiCard card : cards) {
                builder.append("- ").append(card.getTitle())
                        .append(" | ").append(card.getSubtitle())
                        .append(" | ").append(card.getMeta())
                        .append(" | 价格=").append(card.getPrice())
                        .append('\n');
            }
        }
        builder.append("请结合以上信息生成 1 段适合前端直接展示的客服回复文本。");
        return builder.toString();
    }
}
