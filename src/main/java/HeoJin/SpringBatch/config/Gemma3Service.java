package HeoJin.SpringBatch.config;


import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Gemma3Service {

    @Value("${gemini.api.url}")
    private String apiUrl;
    private final RestClient restClient;

    public Gemma3Service( RestClient restClient) {
        this.apiUrl = apiUrl;
        this.restClient = restClient;
    }

    public String generateContent(String prompt) {
        try {
            // Gemma3 API 요청 형식
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String response = restClient.post()
                    .uri(apiUrl)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return response;

        } catch (Exception e) {
            log.error("Error calling Gemma3 API: {}", e.getMessage());
            throw new RuntimeException("Failed to generate content", e);
        }
    }

}
