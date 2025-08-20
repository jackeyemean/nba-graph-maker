package com.nba.service;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphService {
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private DataService dataService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public GraphResponse generateGraph(GraphRequest request) {
        try {
            String analysis = openAIService.analyzeChartRequest(request.getPrompt());
            String realData = queryDatabaseWithAnalysis(analysis);
            String chartConfig = openAIService.generateChartConfig(request.getPrompt(), realData);
            
            return new GraphResponse(chartConfig, null);
            
        } catch (Exception e) {
            return new GraphResponse(null, "Failed to generate chart: " + e.getMessage());
        }
    }
    
    private String queryDatabaseWithAnalysis(String analysis) {
        try {
            return dataService.getDataForAnalysis(analysis);
        } catch (Exception e) {
            return "[]";
        }
    }
}

