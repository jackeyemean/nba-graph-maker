package com.nba.service;

import com.nba.entity.PlayerStats;
import com.nba.repository.PlayerStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class DataService {
    
    @Autowired
    private PlayerStatsRepository playerStatsRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String getSampleData() {
        return """
            NBA Player Statistics Database (1950-2025):
            - Each record represents ONE SEASON for ONE PLAYER
            - All stats are PER-GAME AVERAGES (not totals)
            - Data structure: {season, player_name, team, position, age, points, rebounds, assists, steals, blocks, field_goal_pct, three_pt_pct, turnovers}
            
            AVAILABLE QUERY TYPES:
            1. Single Player Analysis: "Stephen Curry assists" -> line chart of assists per season
            2. Player Comparison: "Derrick Rose vs Kobe Bryant points" -> comparison chart (age vs PPG)
            3. Season Analysis: "2023 season points leaders" -> histogram of top players
            4. Scatter Plots: "2023 season points vs assists" -> scatter plot of all players
            5. Team Analysis: "Lakers 2023 season" -> team player stats
            6. Career Trajectories: "LeBron James career points" -> line chart over seasons
            7. Statistical Correlations: "2023 season field goal percentage vs points" -> scatter plot
            
            CHART TYPE GUIDANCE:
            - Line charts: For time series (seasons, career progression)
            - Histograms: For comparisons (players, teams, seasons)
            - Scatter plots: For correlations (points vs assists, age vs performance)
            - Multi-line charts: For player comparisons (age vs PPG)
            
            IMPORTANT NOTES:
            - Use 'age' for career comparisons (better than season for different career lengths)
            - Use 'season' for year-over-year analysis
            - All stats are per-game averages, not cumulative totals
            """;
    }
    
    public String getPlayerStats(String playerName, String statType) {
        try {
            List<PlayerStats> stats = playerStatsRepository.findByPlayerNameContainingOrderBySeason(playerName);
            
            if (stats.isEmpty()) {
                return "[]";
            }
            
            List<Map<String, Object>> chartData = stats.stream()
                .map(stat -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("season", stat.getSeason());
                    dataPoint.put("player", stat.getPlayer().getName());
                    dataPoint.put("team", stat.getPlayer().getTeam());
                    dataPoint.put("position", stat.getPlayer().getPosition());
                    dataPoint.put("age", stat.getPlayer().getAge());
                    dataPoint.put("points", stat.getPoints());
                    dataPoint.put("rebounds", stat.getRebounds());
                    dataPoint.put("assists", stat.getAssists());
                    dataPoint.put("steals", stat.getSteals());
                    dataPoint.put("blocks", stat.getBlocks());
                    dataPoint.put("field_goal_pct", stat.getFieldGoalPct());
                    dataPoint.put("three_pt_pct", stat.getThreePtPct());
                    dataPoint.put("turnovers", stat.getTurnovers());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(chartData);
            
        } catch (Exception e) {
            return "[]";
        }
    }
    
    public String getTeamStats(String teamName) {
        try {
            List<PlayerStats> stats = playerStatsRepository.findByTeamOrderBySeason(teamName);
            
            List<Map<String, Object>> chartData = stats.stream()
                .map(stat -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("season", stat.getSeason());
                    dataPoint.put("player", stat.getPlayer().getName());
                    dataPoint.put("team", stat.getPlayer().getTeam());
                    dataPoint.put("points", stat.getPoints());
                    dataPoint.put("rebounds", stat.getRebounds());
                    dataPoint.put("assists", stat.getAssists());
                    dataPoint.put("steals", stat.getSteals());
                    dataPoint.put("blocks", stat.getBlocks());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(chartData);
            
        } catch (Exception e) {
            return "[]";
        }
    }
    
    public List<String> searchPlayers(String playerName) {
        return playerStatsRepository.findPlayerNamesContaining(playerName);
    }
    
    public List<String> getAllSeasons() {
        return playerStatsRepository.findAllSeasons();
    }

    public String getPlayerComparison(String player1, String player2, String statType) {
        try {
            List<PlayerStats> stats1 = playerStatsRepository.findByPlayerNameContainingOrderBySeason(player1);
            List<PlayerStats> stats2 = playerStatsRepository.findByPlayerNameContainingOrderBySeason(player2);
            
            if (stats1.isEmpty() && stats2.isEmpty()) {
                return "[]";
            }
            
            List<Map<String, Object>> chartData = new ArrayList<>();
            
            for (PlayerStats stat : stats1) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("player", stat.getPlayer().getName());
                dataPoint.put("age", stat.getPlayer().getAge());
                dataPoint.put("season", stat.getSeason());
                dataPoint.put("team", stat.getPlayer().getTeam());
                dataPoint.put("points", stat.getPoints());
                dataPoint.put("rebounds", stat.getRebounds());
                dataPoint.put("assists", stat.getAssists());
                dataPoint.put("steals", stat.getSteals());
                dataPoint.put("blocks", stat.getBlocks());
                dataPoint.put("field_goal_pct", stat.getFieldGoalPct());
                dataPoint.put("three_pt_pct", stat.getThreePtPct());
                dataPoint.put("turnovers", stat.getTurnovers());
                chartData.add(dataPoint);
            }
            
            for (PlayerStats stat : stats2) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("player", stat.getPlayer().getName());
                dataPoint.put("age", stat.getPlayer().getAge());
                dataPoint.put("season", stat.getSeason());
                dataPoint.put("team", stat.getPlayer().getTeam());
                dataPoint.put("points", stat.getPoints());
                dataPoint.put("rebounds", stat.getRebounds());
                dataPoint.put("assists", stat.getAssists());
                dataPoint.put("steals", stat.getSteals());
                dataPoint.put("blocks", stat.getBlocks());
                dataPoint.put("field_goal_pct", stat.getFieldGoalPct());
                dataPoint.put("three_pt_pct", stat.getThreePtPct());
                dataPoint.put("turnovers", stat.getTurnovers());
                chartData.add(dataPoint);
            }
            
            return objectMapper.writeValueAsString(chartData);
            
        } catch (Exception e) {
            return "[]";
        }
    }
    
    public String getSeasonAnalysis(String season, String statType, int limit) {
        try {
            List<PlayerStats> stats = playerStatsRepository.findBySeasonOrderByPointsDesc(season);
            
            if (stats.isEmpty()) {
                return "[]";
            }
            
            List<PlayerStats> topStats = stats.stream().limit(limit).collect(Collectors.toList());
            
            List<Map<String, Object>> chartData = topStats.stream()
                .map(stat -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("player", stat.getPlayer().getName());
                    dataPoint.put("team", stat.getPlayer().getTeam());
                    dataPoint.put("position", stat.getPlayer().getPosition());
                    dataPoint.put("age", stat.getPlayer().getAge());
                    dataPoint.put("points", stat.getPoints());
                    dataPoint.put("rebounds", stat.getRebounds());
                    dataPoint.put("assists", stat.getAssists());
                    dataPoint.put("steals", stat.getSteals());
                    dataPoint.put("blocks", stat.getBlocks());
                    dataPoint.put("field_goal_pct", stat.getFieldGoalPct());
                    dataPoint.put("three_pt_pct", stat.getThreePtPct());
                    dataPoint.put("turnovers", stat.getTurnovers());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(chartData);
            
        } catch (Exception e) {
            return "[]";
        }
    }
    
    public String getScatterPlotData(String season, String xStat, String yStat) {
        try {
            List<String> availableSeasons = getAllSeasons();
            List<PlayerStats> stats = playerStatsRepository.findBySeason(season);
            
            if (stats.isEmpty() && !availableSeasons.isEmpty()) {
                String mostRecentSeason = availableSeasons.get(availableSeasons.size() - 1);
                stats = playerStatsRepository.findBySeason(mostRecentSeason);
            }
            
            if (stats.isEmpty()) {
                return "[]";
            }
            
            List<Map<String, Object>> chartData = stats.stream()
                .map(stat -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("player", stat.getPlayer().getName());
                    dataPoint.put("team", stat.getPlayer().getTeam());
                    dataPoint.put("position", stat.getPlayer().getPosition());
                    dataPoint.put("age", stat.getPlayer().getAge());
                    dataPoint.put("points", stat.getPoints());
                    dataPoint.put("rebounds", stat.getRebounds());
                    dataPoint.put("assists", stat.getAssists());
                    dataPoint.put("steals", stat.getSteals());
                    dataPoint.put("blocks", stat.getBlocks());
                    dataPoint.put("field_goal_pct", stat.getFieldGoalPct());
                    dataPoint.put("three_pt_pct", stat.getThreePtPct());
                    dataPoint.put("turnovers", stat.getTurnovers());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(chartData);
            
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getDataForAnalysis(String analysis) {
        try {
            JsonNode analysisJson = objectMapper.readTree(analysis);
            String playerName = analysisJson.get("playerName").asText();
            String statType = analysisJson.get("statType").asText();
            String chartType = analysisJson.get("chartType").asText();
            String timePeriod = analysisJson.get("timePeriod").asText();
            
            if (playerName.contains("vs") || playerName.contains(",") || playerName.contains("and")) {
                return handlePlayerComparison(playerName, statType);
            } else if (timePeriod.contains("202") || timePeriod.contains("season") || timePeriod.contains("year")) {
                return handleSeasonAnalysis(timePeriod, statType);
            } else if (chartType.contains("scatter") || statType.contains("vs")) {
                return handleScatterPlot(timePeriod, statType);
            } else if (playerName.toLowerCase().contains("team") || playerName.toLowerCase().contains("lakers") || playerName.toLowerCase().contains("warriors")) {
                return handleTeamAnalysis(playerName);
            } else {
                return handleSinglePlayerAnalysis(playerName, statType);
            }
            
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private String handlePlayerComparison(String playerName, String statType) {
        String[] players = extractPlayerNames(playerName);
        if (players.length >= 2) {
            return getPlayerComparison(players[0], players[1], statType);
        }
        return "[]";
    }
    
    private String handleSeasonAnalysis(String timePeriod, String statType) {
        String season = extractSeason(timePeriod);
        
        if (statType.contains("vs") || statType.contains("scatter")) {
            return getScatterPlotData(season, "age", "points");
        } else {
            return getSeasonAnalysis(season, statType, 20);
        }
    }
    
    private String handleScatterPlot(String timePeriod, String statType) {
        String[] stats = extractStats(statType);
        if (stats.length >= 2) {
            String season = extractSeason(timePeriod);
            return getScatterPlotData(season, stats[0], stats[1]);
        }
        return "[]";
    }
    
    private String handleTeamAnalysis(String playerName) {
        String team = extractTeam(playerName);
        return getTeamStats(team);
    }
    
    private String handleSinglePlayerAnalysis(String playerName, String statType) {
        return getPlayerStats(playerName, statType);
    }
    
    private String[] extractPlayerNames(String playerNameString) {
        if (playerNameString.contains(" vs ")) {
            String[] parts = playerNameString.split(" vs ");
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        
        if (playerNameString.contains(" and ")) {
            String[] parts = playerNameString.split(" and ");
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        
        if (playerNameString.contains(",")) {
            String[] parts = playerNameString.split(",");
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        
        return new String[]{"", ""};
    }
    
    private String extractSeason(String analysis) {
        if (analysis.contains("2025")) return "2025";
        if (analysis.contains("2024")) return "2024";
        if (analysis.contains("2023")) return "2023";
        if (analysis.contains("2022")) return "2022";
        if (analysis.contains("2021")) return "2021";
        if (analysis.contains("2020")) return "2020";
        if (analysis.contains("2019")) return "2019";
        if (analysis.contains("2018")) return "2018";
        if (analysis.contains("2017")) return "2017";
        if (analysis.contains("2016")) return "2016";
        if (analysis.contains("2015")) return "2015";
        if (analysis.contains("2014")) return "2014";
        if (analysis.contains("2013")) return "2013";
        if (analysis.contains("2012")) return "2012";
        if (analysis.contains("2011")) return "2011";
        if (analysis.contains("2010")) return "2010";
        
        try {
            List<String> availableSeasons = getAllSeasons();
            if (!availableSeasons.isEmpty()) {
                return availableSeasons.get(availableSeasons.size() - 1);
            }
        } catch (Exception e) {
            // Fallback to default
        }
        
        return "2023";
    }
    
    private String extractStat(String analysis) {
        if (analysis.contains("points")) return "points";
        if (analysis.contains("assists")) return "assists";
        if (analysis.contains("rebounds")) return "rebounds";
        if (analysis.contains("steals")) return "steals";
        if (analysis.contains("blocks")) return "blocks";
        if (analysis.contains("field_goal_pct")) return "field_goal_pct";
        if (analysis.contains("three_pt_pct")) return "three_pt_pct";
        if (analysis.contains("turnovers")) return "turnovers";
        return "points";
    }
    
    private String[] extractStats(String analysis) {
        if (analysis.contains("points") && analysis.contains("assists")) {
            return new String[]{"points", "assists"};
        }
        if (analysis.contains("points") && analysis.contains("rebounds")) {
            return new String[]{"points", "rebounds"};
        }
        if (analysis.contains("assists") && analysis.contains("rebounds")) {
            return new String[]{"assists", "rebounds"};
        }
        return new String[]{"points", "assists"};
    }
    
    private String extractTeam(String analysis) {
        if (analysis.contains("Lakers")) return "Lakers";
        if (analysis.contains("Warriors")) return "Warriors";
        if (analysis.contains("Celtics")) return "Celtics";
        if (analysis.contains("Bulls")) return "Bulls";
        if (analysis.contains("Heat")) return "Heat";
        if (analysis.contains("Knicks")) return "Knicks";
        if (analysis.contains("Nets")) return "Nets";
        if (analysis.contains("Clippers")) return "Clippers";
        if (analysis.contains("Suns")) return "Suns";
        if (analysis.contains("Mavericks")) return "Mavericks";
        return "Lakers";
    }
}

