package com.nba.dto;

public class GraphResponse {
    private String chartData;
    private String error;

    public GraphResponse() {}

    public GraphResponse(String chartData, String error) {
        this.chartData = chartData;
        this.error = error;
    }

    public String getChartData() {
        return chartData;
    }

    public void setChartData(String chartData) {
        this.chartData = chartData;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

