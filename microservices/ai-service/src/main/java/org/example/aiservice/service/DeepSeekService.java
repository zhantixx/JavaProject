package org.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class DeepSeekService {

    private final WebClient.Builder webClientBuilder;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    public DeepSeekService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public String chatWithAI(String userMessage) {
        return callDeepSeekAPI(userMessage);
    }

    public synchronized void updateConfig(String apiUrl, String model, String apiKey) {
        if (apiUrl != null && !apiUrl.isBlank()) {
            this.apiUrl = apiUrl.trim();
        }
        if (model != null && !model.isBlank()) {
            this.model = model.trim();
        }
        if (apiKey != null && !apiKey.isBlank()) {
            this.apiKey = apiKey.trim();
        }
    }

    public synchronized void setApiUrl(String apiUrl) {
        this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl").trim();
    }

    public synchronized void setModel(String model) {
        this.model = Objects.requireNonNull(model, "model").trim();
    }

    public synchronized void setApiKey(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey").trim();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }

    public String getApiKey() {
        return apiKey;
    }

    private String callDeepSeekAPI(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return buildFallbackAdvice(prompt, "AI кілті бапталмаған");
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content", "Сен жеке ассистентсің. Қазақ тілінде қысқа, анық және пайдалы жауап бер. Егер тапсырма күрделі болса, оны қадамдарға бөл."),
                    Map.of("role", "user", "content", prompt)
            });
            requestBody.put("stream", false);

            String responseJson = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            if (contentNode == null || contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return buildFallbackAdvice(prompt, "AI бос жауап қайтарды");
            }
            return contentNode.asText();
        } catch (WebClientResponseException.Unauthorized ex) {
            return buildFallbackAdvice(prompt, "DeepSeek авторизациясы сәтсіз өтті");
        } catch (WebClientResponseException.Forbidden ex) {
            return buildFallbackAdvice(prompt, "DeepSeek рұқсат бермеді");
        } catch (Exception ex) {
            return buildFallbackAdvice(prompt, "AI сервисіне қосылу уақытша мүмкін болмады");
        }
    }

    private String buildFallbackAdvice(String prompt, String reason) {
        String normalized = prompt == null ? "" : prompt.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        boolean planning = lower.contains("жоспар")
                || lower.contains("step")
                || lower.contains("қадам")
                || lower.contains("task")
                || lower.contains("тапсырма")
                || lower.contains("сложн")
                || lower.contains("күрделі");

        if (planning) {
            return """
                    AI уақытша шектеулі режимде жұмыс істеп тұр: %s.

                    Күрделі тапсырманы былай бөлуді ұсынамын:
                    1) Нәтижені нақтылаңыз: соңында не дайын болуы керек екенін бір сөйлеммен жазыңыз.
                    2) Тапсырманы 3-5 шағын бөлікке бөліңіз.
                    3) Алғашқы 30-60 минутта орындалатын ең жеңіл қадамнан бастаңыз.
                    4) Әр қадамға жеке уақыт бекітіңіз.
                    5) Соңында тексеру және түзетуге бөлек уақыт қалдырыңыз.

                    Қысқа кеңес:
                    - Егер іс түсініксіз болса, алдымен зерттеу, сосын орындау деп екіге бөліңіз.
                    - Егер іс үлкен болса, оны бүгінгі қадам және кейінгі қадам деп ажыратыңыз.
                    - Егер мерзім жақын болса, сапасы жоғары ең маңызды бөлігін бірінші аяқтаңыз.
                    """.formatted(reason);
        }

        return """
                AI уақытша шектеулі режимде жұмыс істеп тұр: %s.

                Қазір мынадай қысқа тәсілді қолданыңыз:
                1) Мақсатты бір сөйлеммен жазыңыз.
                2) Келесі нақты әрекетті таңдаңыз.
                3) Оған шекті уақыт қойыңыз.
                4) Аяқтағаннан кейін келесі қадамды ғана анықтаңыз.
                """.formatted(reason);
    }
}
