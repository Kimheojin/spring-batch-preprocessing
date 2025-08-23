package HeoJin.SpringBatch.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeminiConfig {

//    https://docs.aimlapi.com/api-references/text-models-llm/google/gemma-3
    // 여기가 그나마 정확한듯

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.timeout:30000}")
    private int timeout;


    @Bean
    public RestClient gemma3RestClient() {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }


    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    @Bean
    public Gemma3Service gemma3Service() {
        return new Gemma3Service(gemma3RestClient());
    }
}
