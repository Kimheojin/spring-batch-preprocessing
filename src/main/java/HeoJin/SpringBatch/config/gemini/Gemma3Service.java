package HeoJin.SpringBatch.config.gemini;


import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Gemma3Service {

    @Value("${gemini.api.url}")
    private String apiUrl;


    @Value("${prompt.test}")
    private String testPrompt;


    @Value("${recipe.deploy.processedDB}")
    private String processedCollection;
    private final RestClient restClient;

    public Gemma3Service( RestClient restClient) {
        this.restClient = restClient;
    }

    //https://ai.google.dev/gemma/docs/core/gemma_on_gemini_api?hl=ko#rest
    // REST 형식 관련

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
            // Batch 에러 처리 방식보고 다시 짜야할듯
            // 메타 테이블 관련 해서 더 보기
            log.error("Error calling Gemma3 API: {}", e.getMessage());
            throw new RuntimeException("Failed to generate content", e);
        }
    }
    public  List<ProcessedRecipe> processBatch(List<RawRecipe> items) throws JsonProcessingException {
        log.info("processor 시작");



        ObjectMapper objectMapper = new ObjectMapper();
        String recipesJson = objectMapper.writeValueAsString(items);
        String prompt = testPrompt + recipesJson;

        String response = generateContent(prompt);
        JsonNode responseNode = objectMapper.readTree(response);
        String actualData = responseNode.get("candidates").get(0)
                .get("content").get("parts").get(0)
                .get("text").asText();

        // "```json\n"과 "\n```" 제거
        String cleanedData = actualData.replaceAll("```json\\n", "").replaceAll("\\n```", "");

        log.info("정제된 데이터: {}", cleanedData);
        List<ProcessedRecipe> processedRecipes = objectMapper.readValue(
                cleanedData,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ProcessedRecipe.class)
        );




        return processedRecipes;
    }


}
