package com.aishwarya.aishwarya_mart.dto;

public class ApiResponseDTO<T> {
    private boolean success;
    private T data;
    private ErrorDetail error;

    public ApiResponseDTO(boolean success, T data, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(true, data, null);
    }

    public static <T> ApiResponseDTO<T> failure(String code, String message) {
        return new ApiResponseDTO<>(false, null, new ErrorDetail(code, message));
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorDetail getError() { return error; }

    public static class ErrorDetail {
        private String code;
        private String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}