package org.example.aiservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aiservice.dto.RecommendationItemResponse;
import org.example.aiservice.dto.RecommendationNoteInput;
import org.example.aiservice.dto.RecommendationRequest;
import org.example.aiservice.dto.RecommendationResponse;
import org.example.aiservice.dto.RecommendationTaskInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeepSeekService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public RecommendationResponse buildRecommendations(RecommendationRequest request) {
        RecommendationRequest safeRequest = request == null ? new RecommendationRequest() : request;

        if (apiKey == null || apiKey.isBlank()) {
            return buildFallbackRecommendations(safeRequest, "local");
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();
            String prompt = buildRecommendationPrompt(safeRequest);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content", """
                            Сен SanaBot жүйесіндегі AI ассистентсің.
                            Пайдаланушыға қазақ тілінде қысқа, нақты және іс-әрекетке бағытталған ұсыныстар бер.
                            Жауапты тек JSON түрінде қайтар:
                            {
                              "summary": "қысқа қорытынды",
                              "recommendations": [
                                {"title":"...", "detail":"...", "action":"..."}
                              ]
                            }
                            Ұсыныстар саны 3-тен аспасын.
                            """),
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

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            if (contentNode == null || contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return buildFallbackRecommendations(safeRequest, "local");
            }

            return parseRecommendationResponse(contentNode.asText(), safeRequest);
        } catch (Exception ex) {
            return buildFallbackRecommendations(safeRequest, "local");
        }
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

            JsonNode root = objectMapper.readTree(responseJson);
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

    private RecommendationResponse parseRecommendationResponse(String content, RecommendationRequest request) {
        try {
            String normalized = extractJson(content);
            JsonNode root = objectMapper.readTree(normalized);
            String summary = root.path("summary").asText("").trim();
            List<RecommendationItemResponse> items = objectMapper.convertValue(
                    root.path("recommendations"),
                    new TypeReference<List<RecommendationItemResponse>>() {}
            );

            List<RecommendationItemResponse> cleaned = (items == null ? List.<RecommendationItemResponse>of() : items)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.getTitle() != null && !item.getTitle().isBlank())
                    .limit(3)
                    .collect(Collectors.toList());

            if (cleaned.isEmpty()) {
                return buildFallbackRecommendations(request, "local");
            }

            RecommendationResponse response = new RecommendationResponse();
            response.setMode("ai");
            response.setSummary(summary.isBlank() ? "AI сіздің ағымдағы жүктемеңізге қарай қысқа ұсыныстар дайындады." : summary);
            response.setRecommendations(cleaned);
            return response;
        } catch (Exception ex) {
            return buildFallbackRecommendations(request, "local");
        }
    }

    private String extractJson(String content) {
        String trimmed = content == null ? "" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String buildRecommendationPrompt(RecommendationRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Пайдаланушы: ")
                .append(request.getUserName() == null || request.getUserName().isBlank() ? "Пайдаланушы" : request.getUserName())
                .append("\n");
        builder.append("Тапсырмалар:\n");

        List<RecommendationTaskInput> tasks = request.getTasks() == null ? List.of() : request.getTasks();
        if (tasks.isEmpty()) {
            builder.append("- Тапсырма жоқ\n");
        } else {
            tasks.stream().limit(12).forEach(task -> builder
                    .append("- ")
                    .append(defaultText(task.getTitle(), "Атаусыз тапсырма"))
                    .append(" | priority=")
                    .append(defaultText(task.getPriority(), "MEDIUM"))
                    .append(" | status=")
                    .append(defaultText(task.getStatus(), "PLANNED"))
                    .append(" | deadline=")
                    .append(defaultText(task.getDeadline(), "жоқ"))
                    .append(" | description=")
                    .append(defaultText(task.getDescription(), ""))
                    .append("\n"));
        }

        builder.append("Жазбалар:\n");
        List<RecommendationNoteInput> notes = request.getNotes() == null ? List.of() : request.getNotes();
        if (notes.isEmpty()) {
            builder.append("- Жазба жоқ\n");
        } else {
            notes.stream().limit(6).forEach(note -> builder
                    .append("- ")
                    .append(defaultText(note.getTitle(), "Жазба"))
                    .append(": ")
                    .append(defaultText(note.getContent(), ""))
                    .append("\n"));
        }
        builder.append("Осы деректерге сүйеніп, пайдаланушының фокусына ең пайдалы 3 ұсыныс бер.");
        return builder.toString();
    }

    private RecommendationResponse buildFallbackRecommendations(RecommendationRequest request, String mode) {
        List<RecommendationTaskInput> tasks = request.getTasks() == null ? List.of() : request.getTasks();
        List<RecommendationNoteInput> notes = request.getNotes() == null ? List.of() : request.getNotes();

        long openCount = tasks.stream().filter(task -> !isDone(task)).count();
        long overdueCount = tasks.stream().filter(this::isOverdue).count();
        long highPriorityOpen = tasks.stream().filter(task -> !isDone(task) && isHighPriority(task)).count();
        long noDeadlineOpen = tasks.stream().filter(task -> !isDone(task) && isBlank(task.getDeadline())).count();

        List<RecommendationItemResponse> items = new ArrayList<>();

        if (overdueCount > 0) {
            items.add(new RecommendationItemResponse(
                    "Кешіккен істерді тазалау",
                    "Сізде " + overdueCount + " кешіккен тапсырма бар. Олар фокусты шашыратып, жаңа жоспардың сапасын төмендетеді.",
                    "Бүгін тек кешіккен 1-2 тапсырманы жабуға немесе жаңа мерзім қоюға уақыт бөліңіз."
            ));
        }

        if (highPriorityOpen >= 3) {
            items.add(new RecommendationItemResponse(
                    "Басымдықты тарылту",
                    "Жоғары басымдықтағы ашық тапсырмалар саны " + highPriorityOpen + ". Барлығы бірдей маңызды болса, фокус әлсірейді.",
                    "Олардың ішінен дәл бүгін орындалатын ең маңызды 2 тапсырманы ғана қалдырыңыз."
            ));
        }

        if (noDeadlineOpen > 0) {
            items.add(new RecommendationItemResponse(
                    "Мерзімсіз істерді нақтылау",
                    "Ашық тапсырмалардың " + noDeadlineOpen + " данасында мерзім көрсетілмеген.",
                    "Әр тапсырмаға кемінде апта немесе күн деңгейінде соңғы мерзім қойыңыз."
            ));
        }

        if (!notes.isEmpty()) {
            items.add(new RecommendationItemResponse(
                    "Жазбаны іске айналдыру",
                    "Жазбалар саны " + notes.size() + ". Демек идеялар жиналып тұр, бірақ олардың бәрі әрекетке айналмаған болуы мүмкін.",
                    "Соңғы жазбалардың ішінен бір нақты идеяны таңдап, оны тапсырма немесе келесі қадам ретінде бекітіңіз."
            ));
        }

        if (items.isEmpty()) {
            if (openCount == 0) {
                items.add(new RecommendationItemResponse(
                        "Жаңа фокус орнату",
                        "Қазір белсенді ашық тапсырма көрінбейді. Бұл жоспарды жаңадан жинауға жақсы сәт.",
                        "Аптаға 1 негізгі мақсат, айға 1 нәтиже және бүгінге 1 нақты қадам белгілеңіз."
                ));
            } else {
                items.add(new RecommendationItemResponse(
                        "Бірінші қадамды қысқарту",
                        "Ашық тапсырмалар бар, бірақ ең үлкен кедергі көбіне бастау сәтінде болады.",
                        "Ең маңызды тапсырмаңыз үшін 30 минутта бітетін алғашқы әрекетті бөліп жазыңыз."
                ));
            }
        }

        while (items.size() < 3) {
            if (items.stream().noneMatch(item -> "Апталық шолу".equals(item.getTitle()))) {
                items.add(new RecommendationItemResponse(
                        "Апталық шолу",
                        "Қысқа шолу жүйені тұрақты ұстайды және ұмыт қалған істерді ерте байқатады.",
                        "Аптаның соңында 10 минут бөліп, не бітті, не кешікті және келесі фокус қандай екенін қарап шығыңыз."
                ));
                continue;
            }

            items.add(new RecommendationItemResponse(
                    "Фокус терезесін қорғау",
                    "Өнімділік көбіне тапсырма санынан емес, үзілмейтін уақыт терезесінен өседі.",
                    "Күнтізбеде 45-60 минуттық тыныш жұмыс блогын бекітіп, сол кезде тек бір тапсырмамен жұмыс істеңіз."
            ));
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setMode(mode);
        response.setSummary(buildFallbackSummary(request, openCount, overdueCount, highPriorityOpen));
        response.setRecommendations(items.stream().limit(3).collect(Collectors.toList()));
        return response;
    }

    private String buildFallbackSummary(RecommendationRequest request, long openCount, long overdueCount, long highPriorityOpen) {
        String name = request.getUserName() == null || request.getUserName().isBlank() ? "Сізде" : request.getUserName() + ", сізде";
        return name + " " + openCount + " ашық тапсырма бар, оның " + overdueCount + " кешіккен, ал "
                + highPriorityOpen + " жоғары басымдықта. Осыған сүйеніп, ең пайдалы 3 фокус ұсынысы дайындалды.";
    }

    private boolean isDone(RecommendationTaskInput task) {
        return task != null && "DONE".equalsIgnoreCase(defaultText(task.getStatus(), ""));
    }

    private boolean isHighPriority(RecommendationTaskInput task) {
        return task != null && "HIGH".equalsIgnoreCase(defaultText(task.getPriority(), ""));
    }

    private boolean isOverdue(RecommendationTaskInput task) {
        if (task == null || isBlank(task.getDeadline()) || isDone(task)) {
            return false;
        }
        try {
            return OffsetDateTime.parse(task.getDeadline()).isBefore(OffsetDateTime.now(ZoneOffset.UTC));
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }
}
