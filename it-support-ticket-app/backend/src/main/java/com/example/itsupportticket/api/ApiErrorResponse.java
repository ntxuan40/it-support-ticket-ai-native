package com.example.itsupportticket.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApiErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ApiValidationError> details = new ArrayList<>();

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ApiValidationError> getDetails() {
        return details;
    }

    public void setDetails(List<ApiValidationError> details) {
        this.details = details;
    }

    public static class ApiValidationError {
        private String field;
        private String code;
        private String message;

        public ApiValidationError(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
