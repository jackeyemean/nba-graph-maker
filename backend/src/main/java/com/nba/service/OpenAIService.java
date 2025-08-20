package com.nba.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String analyzeChartRequest(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API key is not configured");
        }
        
        if (apiKey.startsWith("sk-proj-")) {
            return analyzeChartRequestWithProjectKey(prompt);
        } else {
            return analyzeChartRequestWithStandardKey(prompt);
        }
    }
    
    public String generateChartConfig(String prompt, String sampleData) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API key is not configured");
        }
        
        if (apiKey.startsWith("sk-proj-")) {
            return generateChartConfigWithProjectKey(prompt, sampleData);
        } else {
            return generateChartConfigWithStandardKey(prompt, sampleData);
        }
    }
    
    private String analyzeChartRequestWithProjectKey(String prompt) {
        try {
            String systemPrompt = "You are an expert data visualization analyst specializing in NBA statistics. " +
                "Analyze the user's request to understand what type of chart they want and what data is needed. " +
                "Your analysis should guide the data retrieval process. " +
                "ANALYSIS GUIDELINES: " +
                "- For player comparisons (e.g., 'Derrick Rose vs Kobe Bryant'): Identify both players and the stat to compare " +
                "- For season analysis (e.g., '2023 season points leaders'): Identify the season and stat type " +
                "- For correlations (e.g., 'points vs assists'): Identify both stats for scatter plot " +
                "- For career analysis (e.g., 'Stephen Curry assists'): Identify the player and stat type " +
                "Respond with a JSON object containing: playerName, statType, chartType, timePeriod, description. " +
                "For multiple players, separate with 'vs' (e.g., 'Derrick Rose vs Kobe Bryant'). " +
                "For multiple stats, separate with 'vs' (e.g., 'points vs assists').";

            String userPrompt = "Analyze this request: " + prompt;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            });
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 500);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", requestEntity, String.class);
            
            if (response == null) {
                throw new RuntimeException("No response from OpenAI API");
            }

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode choices = responseJson.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

            String content = choices.get(0).get("message").get("content").asText();
            return extractJsonFromResponse(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze chart request: " + e.getMessage());
        }
    }
    
    private String analyzeChartRequestWithStandardKey(String prompt) {
        try {
            String systemPrompt = "You are an expert data visualization analyst specializing in NBA statistics. " +
                "Analyze the user's request to understand what type of chart they want and what data is needed. " +
                "Your analysis should guide the data retrieval process. " +
                "ANALYSIS GUIDELINES: " +
                "- For player comparisons (e.g., 'Derrick Rose vs Kobe Bryant'): Identify both players and the stat to compare " +
                "- For season analysis (e.g., '2023 season points leaders'): Identify the season and stat type " +
                "- For correlations (e.g., 'points vs assists'): Identify both stats for scatter plot " +
                "- For career analysis (e.g., 'Stephen Curry assists'): Identify the player and stat type " +
                "Respond with a JSON object containing: playerName, statType, chartType, timePeriod, description. " +
                "For multiple players, separate with 'vs' (e.g., 'Derrick Rose vs Kobe Bryant'). " +
                "For multiple stats, separate with 'vs' (e.g., 'points vs assists').";

            String userPrompt = "Analyze this request: " + prompt;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            });
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 500);

            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", requestBody, String.class);
            
            if (response == null) {
                throw new RuntimeException("No response from OpenAI API");
            }

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode choices = responseJson.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

            String content = choices.get(0).get("message").get("content").asText();
            return extractJsonFromResponse(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze chart request: " + e.getMessage());
        }
    }
    
    private String generateChartConfigWithProjectKey(String prompt, String sampleData) {
        try {
            String systemPrompt = "You are an expert data visualization specialist with deep knowledge of NBA statistics and Chart.js. " +
                "Your job is to analyze the user's request and the provided database data to create the most appropriate chart. " +
                "You have complete control over chart type selection and axis determination. " +
                "DATABASE CONTEXT: " +
                "- Each record represents ONE SEASON for ONE PLAYER " +
                "- All stats are PER-GAME AVERAGES (not totals) " +
                "- Available fields: Player,Age,Team,Pos,G,FG,FGA,FG%,FT,FTA,FT%,AST,PF,PTS " +
                "CHART TYPE DECISION GUIDELINES: " +
                "- HISTOGRAMS: For season leaders, team comparisons, position analysis " +
                "- MULTI-LINE CHARTS: For player comparisons (age vs PPG), multiple players, stat comparisons " +
                "- LINE CHARTS: For career trajectories, single player time series " +
                "- SCATTER PLOTS: For correlations (age vs points, points vs assists) " +
                "- PIE CHARTS: For distributions (team composition, position breakdown) " +
                "AXIS GUIDELINES: " +
                "- Use 'age' for career comparisons (better than season for different career lengths) " +
                "- Use 'season' for year-over-year analysis " +
                "- For scatter plots, use the two stats as X and Y axes " +
                "- For histograms, use player names on X-axis and stats on Y-axis " +
                "IMPORTANT: Generate ONLY valid JSON that can be directly used with Chart.js. " +
                "The response should be a complete Chart.js configuration object.";

            String userPrompt = "User request: " + prompt + "\n\nDatabase context: " + sampleData + "\n\nGenerate a Chart.js configuration for this request.";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            });
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 1500);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", requestEntity, String.class);
            
            if (response == null) {
                throw new RuntimeException("No response from OpenAI API");
            }

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode choices = responseJson.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

            String content = choices.get(0).get("message").get("content").asText();
            return extractJsonFromResponse(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate chart config: " + e.getMessage());
        }
    }
    
    private String generateChartConfigWithStandardKey(String prompt, String sampleData) {
        try {
            String systemPrompt = "You are an expert data visualization specialist with deep knowledge of NBA statistics and Chart.js. " +
                "Your job is to analyze the user's request and the provided database data to create the most appropriate chart. " +
                "You have complete control over chart type selection and axis determination. " +
                "DATABASE CONTEXT: " +
                "- Each record represents ONE SEASON for ONE PLAYER " +
                "- All stats are PER-GAME AVERAGES (not totals) " +
                "- Available fields: Player,Age,Team,Pos,G,FG,FGA,FG%,FT,FTA,FT%,AST,PF,PTS " +
                "CHART TYPE DECISION GUIDELINES: " +
                "- HISTOGRAMS: For season leaders, team comparisons, position analysis " +
                "- MULTI-LINE CHARTS: For player comparisons (age vs PPG), multiple players, stat comparisons " +
                "- LINE CHARTS: For career trajectories, single player time series " +
                "- SCATTER PLOTS: For correlations (age vs points, points vs assists) " +
                "- PIE CHARTS: For distributions (team composition, position breakdown) " +
                "AXIS GUIDELINES: " +
                "- Use 'age' for career comparisons (better than season for different career lengths) " +
                "- Use 'season' for year-over-year analysis " +
                "- For scatter plots, use the two stats as X and Y axes " +
                "- For histograms, use player names on X-axis and stats on Y-axis " +
                "IMPORTANT: Generate ONLY valid JSON that can be directly used with Chart.js. " +
                "The response should be a complete Chart.js configuration object.";

            String userPrompt = "User request: " + prompt + "\n\nDatabase context: " + sampleData + "\n\nGenerate a Chart.js configuration for this request.";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            });
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 1500);

            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", requestBody, String.class);
            
            if (response == null) {
                throw new RuntimeException("No response from OpenAI API");
            }

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode choices = responseJson.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

            String content = choices.get(0).get("message").get("content").asText();
            return extractJsonFromResponse(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate chart config: " + e.getMessage());
        }
    }
    
    private String extractJsonFromResponse(String response) {
        try {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            
            if (start >= 0 && end > start) {
                String jsonPart = response.substring(start, end);
                String cleanedJson = removeComments(jsonPart);
                
                objectMapper.readTree(cleanedJson);
                return cleanedJson;
            }
            
            throw new RuntimeException("No valid JSON found in response");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract valid JSON from AI response: " + e.getMessage());
        }
    }
    
    private String removeComments(String json) {
        String[] lines = json.split("\n");
        StringBuilder cleaned = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("//") && !trimmed.isEmpty()) {
                cleaned.append(line).append("\n");
            }
        }
        
        return cleaned.toString().replaceAll("/\\*.*?\\*/", "").trim();
    }
}

