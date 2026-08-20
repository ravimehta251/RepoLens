package com.reposage.client;

import com.pgvector.hibernate.Vector;
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
public class EmbeddingClientImpl implements EmbeddingClient {
  @Value("${gemini.api-key}")
  private String apiKey;

  @Value("${gemini.api-base}")
  private String apiBase;

  @Value("${gemini.embedding-model}")
  private String embeddingModel;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public EmbeddingClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Vector embed(String text) throws Exception {
    return new Vector(embedAsFloatArray(text));
  }

  @Override
  public float[] embedAsFloatArray(String text) throws Exception {
    String url = apiBase + "/" + embeddingModel + ":embedContent?key=" + apiKey;

    String requestBody = objectMapper.writeValueAsString(new EmbedRequest(text));

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        new HttpEntity<>(requestBody, buildHeaders()),
        String.class
    );

    JsonNode rootNode = objectMapper.readTree(response.getBody());
    JsonNode embedding = rootNode.path("embedding").path("values");

    float[] result = new float[embedding.size()];
    for (int i = 0; i < embedding.size(); i++) {
      result[i] = embedding.get(i).floatValue();
    }

    return result;
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    return headers;
  }

  private record EmbedRequest(String text) {
    @Override
    public String toString() {
      return "{\"text\": \"" + text.replace("\"", "\\\"") + "\"}";
    }
  }
}
