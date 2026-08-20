package com.reposage.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class LLMClientImpl implements LLMClient {
  @Value("${gemini.api-key}")
  private String apiKey;

  @Value("${gemini.api-base}")
  private String apiBase;

  @Value("${gemini.chat-model}")
  private String chatModel;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public LLMClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public String chat(String prompt) throws Exception {
    String url = apiBase + "/" + chatModel + ":generateContent?key=" + apiKey;

    ChatRequest request = new ChatRequest(
        new ChatRequest.Content[]{
            new ChatRequest.Content(
                new ChatRequest.Part[]{
                    new ChatRequest.Part(prompt)
                }
            )
        }
    );

    String requestBody = objectMapper.writeValueAsString(request);

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        new HttpEntity<>(requestBody, buildHeaders()),
        String.class
    );

    JsonNode rootNode = objectMapper.readTree(response.getBody());
    String answer = rootNode
        .path("candidates")
        .get(0)
        .path("content")
        .path("parts")
        .get(0)
        .path("text")
        .asText();

    return answer;
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    return headers;
  }

  private record ChatRequest(Content[] contents) {
    public record Content(Part[] parts) {}
    public record Part(String text) {}
  }
}
