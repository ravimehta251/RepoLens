package com.reposage.client;

import com.pgvector.hibernate.Vector;

public interface EmbeddingClient {
  Vector embed(String text) throws Exception;
  
  float[] embedAsFloatArray(String text) throws Exception;
}
