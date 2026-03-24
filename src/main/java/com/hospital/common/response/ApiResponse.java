package com.hospital.common.response;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T result;   // data → result 변경

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public static <T> ApiResponse<T> success(T result, String message) {
        return new ApiResponse<>(true, message, result);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {   // getData → getResult 변경
        return result;
    }
}
