package com.example.transactions.dto;

/**
 * DTO para respuestas de estado (usado en PUT).
 */
public class StatusResponse {
    private String status;

    public StatusResponse() {
    }

    public StatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
