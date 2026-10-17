package org.ltdz.taskflow.common;

import lombok.Getter;

/**
 * 统一响应结构：{status, message, data}。
 * <p>
 * 统一通过内部构建器 {@link Builder} 构造，避免各处重复 new。
 *
 * @param <T> 业务数据类型
 */
@Getter

public class ApiResponse<T> {

    private Integer status;
    private String message;
    private T data;

    public static<T> Builder<T> builder(){
        return new Builder<>();
    }

    public static class Builder<T> {

        private Integer status;
        private String message;
        private T data;

        private Builder() {
        }
        public Builder<T> status(Integer status) {
            this.status = status;
            return this;
        }
        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }
        public ApiResponse<T> build() {
            ApiResponse<T> apiResponse = new ApiResponse<>();
            apiResponse.status = status;
            apiResponse.message = message;
            apiResponse.data = data;
            return apiResponse;
        }

    }
}
