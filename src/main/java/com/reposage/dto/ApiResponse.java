package com.reposage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private String error;

  public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .error(null)
        .build();
  }

  public static <T> ApiResponse<T> fail(String error) {
    return ApiResponse.<T>builder()
        .success(false)
        .data(null)
        .error(error)
        .build();
  }
}
