package com.reposage.client;

public interface LLMClient {
  String chat(String prompt) throws Exception;
}
