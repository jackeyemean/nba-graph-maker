package com.nba.dto;

public class GraphRequest {
    private String prompt;

    public GraphRequest() {}

    public GraphRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}

