package com.nba.service;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;

import com.nba.entity.PlayerStats;
import com.nba.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.Instant;

@Service
public class GraphService {
    
    @Autowired
    private PlayerStatsRepository playerStatsRepository;
    
    // Performance tracking
    private long startTime;
    private long endTime;
    private int totalRecordsProcessed;
    private int sqlQueriesCount = 0;


    
    public GraphResponse generateGraph(GraphRequest request) {
        // Reset performance tracking
        startTime = System.currentTimeMillis();
        totalRecordsProcessed = 0;
        sqlQueriesCount = 0;
        
        GraphResponse response = new GraphResponse();
        response.setGraphType(request.getGraphType());
        
        // Set default values for missing fields
        switch (request.getGraphType()) {
            case "line":
                // Use default values if not provided
                String xAxisType = request.getXAxisType();
                String yAxisType = request.getYAxisType();
                
                if (xAxisType == null) {
                    xAxisType = "age";
                    request.setXAxisType(xAxisType);
                }
                if (yAxisType == null) {
                    yAxisType = "points";
                    request.setYAxisType(yAxisType);
                }
                break;
                         case "histogram":
                 // Use default values if not provided
                 String stat = request.getStat();
                 if (stat == null) {
                     stat = "points";
                     request.setStat(stat);
                 }
                 break;
            case "scatter":
                // Use default values if not provided
                String xAxisStat = request.getXAxisStat();
                String yAxisStat = request.getYAxisStat();
                
                if (xAxisStat == null) {
                    xAxisStat = "steals";
                    request.setXAxisStat(xAxisStat);
                }
                if (yAxisStat == null) {
                    yAxisStat = "blocks";
                    request.setYAxisStat(yAxisStat);
                }
                break;
        }

        GraphResponse result;
        switch (request.getGraphType()) {
            case "line":
                result = generateLineGraph(request, response);
                break;
            case "histogram":
                result = generateHistogram(request, response);
                break;
            case "scatter":
                result = generateScatterPlot(request, response);
                break;
            default:
                throw new IllegalArgumentException("Unsupported graph type: " + request.getGraphType());
        }
        
        // Add performance metrics
        endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        Map<String, Object> metadata = result.getMetadata() != null ? result.getMetadata() : new HashMap<>();
        
        metadata.put("performance", Map.of(
            "executionTimeMs", executionTime,
            "executionTimeSeconds", String.format("%.2f", executionTime / 1000.0),
            "totalRecordsProcessed", totalRecordsProcessed,
            "sqlQueriesCount", sqlQueriesCount
        ));
        result.setMetadata(metadata);
        
        return result;
    }

    private GraphResponse generateLineGraph(GraphRequest request, GraphResponse response) {
        List<GraphResponse.LineDataset> datasets = new ArrayList<>();
        
        if (request.getPlayers() != null) {
            for (int i = 0; i < request.getPlayers().size(); i++) {
                String player = request.getPlayers().get(i);
                List<PlayerStats> playerData = getPlayerData(player, request);
    
                
                List<Double> xValues = new ArrayList<>();
                List<Double> yValues = new ArrayList<>();
                
                for (PlayerStats stat : playerData) {
                    Double xValue = getValueForAxis(stat, request.getXAxisType());
                    Double yValue = getValueForAxis(stat, request.getYAxisType());
                    

                    
                    if (xValue != null && yValue != null) {
                        xValues.add(xValue);
                        yValues.add(yValue);
                    }
                }
                
                // Sort by X-axis values to ensure proper line graph progression
                // Each player should have their own independent X-axis progression
                List<Double> sortedXValues = new ArrayList<>(xValues);
                List<Double> sortedYValues = new ArrayList<>(yValues);
                
                // Create pairs and sort by X value for this specific player
                List<Map.Entry<Double, Double>> pairs = new ArrayList<>();
                for (int j = 0; j < xValues.size(); j++) {
                    pairs.add(Map.entry(xValues.get(j), yValues.get(j)));
                }
                pairs.sort(Map.Entry.comparingByKey());
                
                sortedXValues.clear();
                sortedYValues.clear();
                for (Map.Entry<Double, Double> pair : pairs) {
                    sortedXValues.add(pair.getKey());
                    sortedYValues.add(pair.getValue());
                }
                

                
                if (!sortedXValues.isEmpty()) {
                    GraphResponse.LineDataset dataset = new GraphResponse.LineDataset();
                    dataset.setLabel(player);
                    dataset.setXValues(sortedXValues);
                    dataset.setYValues(sortedYValues);
                    dataset.setBorderColor(getColorForIndex(i));
                    dataset.setFill(false);
                    datasets.add(dataset);
                }
            }
        }
        
        response.setDatasets(datasets);
        
        // Add metadata for frontend axis labels
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("xAxisType", request.getXAxisType());
        metadata.put("yAxisType", request.getYAxisType());
        response.setMetadata(metadata);
        
        return response;
    }

    private GraphResponse generateHistogram(GraphRequest request, GraphResponse response) {
        List<PlayerStats> data = new ArrayList<>();
        
        // Handle multiple seasons for histograms
        if (request.getYears() != null && !request.getYears().isEmpty()) {
            for (Integer year : request.getYears()) {
                List<PlayerStats> yearData = getSeasonData(year, request.getStat(), request);
                data.addAll(yearData);
            }
        } else if (request.getYear() != null) {
            data = getSeasonData(request.getYear(), request.getStat(), request);
        }
        
        // Optimized histogram binning - single pass through data
        int binCount = request.getBinCount() != null ? request.getBinCount() : 20;
        
        // Pre-calculate all values and find min/max in single pass
        List<Double> values = new ArrayList<>();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        int totalPlayers = data.size();
        int validPlayers = 0;
        
        for (PlayerStats stat : data) {
            Double value = getValueForStat(stat, request.getStat());
            if (value != null) {
                values.add(value);
                validPlayers++;
                if (value < min) min = value;
                if (value > max) max = value;
            }
        }
        
        if (values.isEmpty()) {
            response.setBinEdges(new ArrayList<>());
            response.setBinCounts(new ArrayList<>());
            return response;
        }
        
        double binWidth = (max - min) / binCount;
        
        // Initialize bins
        List<Double> binEdges = new ArrayList<>();
        List<Integer> binCounts = new ArrayList<>();
        List<List<String>> binPlayers = new ArrayList<>();
        
        for (int i = 0; i <= binCount; i++) {
            binEdges.add(min + i * binWidth);
        }
        
        // Initialize bin counts and player lists
        for (int i = 0; i < binCount; i++) {
            binCounts.add(0);
            binPlayers.add(new ArrayList<>());
        }
        
        // Single pass binning - much faster than multiple stream operations
        for (PlayerStats stat : data) {
            Double value = getValueForStat(stat, request.getStat());
            if (value != null) {
                int binIndex = (int) Math.min((value - min) / binWidth, binCount - 1);
                if (binIndex >= 0 && binIndex < binCount) {
                    binCounts.set(binIndex, binCounts.get(binIndex) + 1);
                    binPlayers.get(binIndex).add(stat.getPlayer() + " (" + stat.getYear() + ") - " + String.format("%.1f", value));
                }
            }
        }
        
        response.setBinEdges(binEdges);
        response.setBinCounts(binCounts);
        
        // Add metadata for frontend axis labels and tooltips
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("binPlayers", binPlayers);
        metadata.put("stat", request.getStat());
        
        // Add data availability warnings
        if (validPlayers < totalPlayers) {
            Map<String, Object> warnings = new HashMap<>();
            warnings.put("totalPlayers", totalPlayers);
            warnings.put("validPlayers", validPlayers);
            
            String warningMessage = String.format(
                "Note: %d out of %d players excluded due to missing %s data. " +
                "This may be because some years don't track this statistic.",
                totalPlayers - validPlayers, totalPlayers, getAxisLabel(request.getStat())
            );
            warnings.put("message", warningMessage);
            
            metadata.put("dataWarnings", warnings);
        }
        
        response.setMetadata(metadata);
        
        return response;
    }

    private GraphResponse generateScatterPlot(GraphRequest request, GraphResponse response) {
        List<PlayerStats> data = new ArrayList<>();
        
        // Handle multiple seasons for scatter plots
        if (request.getYears() != null && !request.getYears().isEmpty()) {
            for (Integer year : request.getYears()) {
                List<PlayerStats> yearData = getSeasonData(year, null, request);
                data.addAll(yearData);
            }
        } else if (request.getYear() != null) {
            data = getSeasonData(request.getYear(), null, request);
        }
        
        // Use the values from the request (fallbacks already applied in generateGraph)
        String xAxisStat = request.getXAxisStat();
        String yAxisStat = request.getYAxisStat();
        

        
        List<GraphResponse.ScatterPoint> points = new ArrayList<>();
        Set<String> uniquePlayers = new HashSet<>(); // Track unique players for the list
        
        int totalPlayers = data.size();
        int validPlayers = 0;
        int missingXAxis = 0;
        int missingYAxis = 0;
        
                for (PlayerStats stat : data) {
            Double xValue = getValueForStat(stat, xAxisStat);
            Double yValue = getValueForStat(stat, yAxisStat);
            
            if (xValue == null) {
                missingXAxis++;
            }
            if (yValue == null) {
                missingYAxis++;
            }
            
            if (xValue != null && yValue != null) {
                validPlayers++;
                GraphResponse.ScatterPoint point = new GraphResponse.ScatterPoint();
                point.setX(xValue);
                point.setY(yValue);
                point.setPlayer(stat.getPlayer());
                point.setTeam(stat.getTeam());
                point.setYear(stat.getYear());
                point.setLabel(stat.getPlayer());
                point.setColor("#D3D3D3"); // Light gray color for all points
                points.add(point);
                
                // Add to unique players set
                uniquePlayers.add(stat.getPlayer());
            }
        }
        

        response.setPoints(points);
        
        // Add metadata for frontend axis labels and player list
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("xAxisStat", xAxisStat);
        metadata.put("yAxisStat", yAxisStat);
        
        // Add data availability warnings
        if (missingXAxis > 0 || missingYAxis > 0) {
            Map<String, Object> warnings = new HashMap<>();
            warnings.put("totalPlayers", totalPlayers);
            warnings.put("validPlayers", validPlayers);
            warnings.put("missingXAxis", missingXAxis);
            warnings.put("missingYAxis", missingYAxis);
            
            String warningMessage = String.format(
                "Note: %d out of %d players excluded due to missing data. " +
                "(%d missing %s data, %d missing %s data). " +
                "This may be because some years don't track these statistics.",
                totalPlayers - validPlayers, totalPlayers, 
                missingXAxis, getAxisLabel(xAxisStat),
                missingYAxis, getAxisLabel(yAxisStat)
            );
            warnings.put("message", warningMessage);
            
            metadata.put("dataWarnings", warnings);
        }
        
        // Create sorted player list for display
        List<String> playerList = uniquePlayers.stream()
            .sorted()
            .collect(Collectors.toList());
        metadata.put("playerList", playerList);
        
        response.setMetadata(metadata);
        
        return response;
    }

    private List<PlayerStats> getPlayerData(String playerName, GraphRequest request) {
        sqlQueriesCount++;
        List<PlayerStats> data = playerStatsRepository.findPlayerAllStats(playerName);
        totalRecordsProcessed += data.size();
        
        if (data.isEmpty()) {
            // Try a case-insensitive search
            List<String> allPlayers = playerStatsRepository.findAllPlayers();

        }
        
        // Apply games played filter if specified
        if (request.getMinGamesPlayed() != null && request.getMinGamesPlayed() > 0) {
            data = data.stream()
                .filter(stat -> stat.getGamesPlayed() != null && stat.getGamesPlayed() >= request.getMinGamesPlayed())
                .collect(Collectors.toList());

        }
        
        // Apply minutes per game filter if specified
        // Note: Minutes per game wasn't tracked until 1952, so we need to handle null values
        if (request.getMinMinutesPerGame() != null && request.getMinMinutesPerGame() > 0) {
            data = data.stream()
                .filter(stat -> {
                    // If minutes data is null (pre-1952), include the player
                    if (stat.getMinutesPerGame() == null) {
                        return true;
                    }
                    // If minutes data exists, apply the filter
                    return stat.getMinutesPerGame() >= request.getMinMinutesPerGame();
                })
                .collect(Collectors.toList());

        }
        
        // For line graphs, always use multi-team overall stats (filter out individual team records)
        // First, identify players who have multi-team overall records
        Set<String> playersWithMultiTeamRecords = data.stream()
            .filter(stat -> stat.getTeam() != null && stat.getTeam().contains("TM"))
            .map(stat -> stat.getPlayer() + "_" + stat.getYear())
            .collect(Collectors.toSet());
        
        // Then filter the data
        data = data.stream()
            .filter(stat -> {
                // Keep records where team is null (single team) or contains "TM" (multi-team overall)
                if (stat.getTeam() == null) {
                    return true; // Single team player
                }
                if (stat.getTeam().contains("TM")) {
                    return true; // Multi-team overall stats
                }
                // For individual team records, check if this player has a multi-team overall record
                String playerYearKey = stat.getPlayer() + "_" + stat.getYear();
                return !playersWithMultiTeamRecords.contains(playerYearKey); // Keep if no multi-team record exists
            })
            .collect(Collectors.toList());
        
        return data;
    }

    private List<PlayerStats> getSeasonData(Integer year, String statName, GraphRequest request) {
        List<PlayerStats> data;
        
        // Use optimized query with all filters in one database call
        List<String> positions = (request.getPositions() != null && !request.getPositions().isEmpty() && !request.getPositions().contains("All")) 
            ? request.getPositions() : null;
        List<String> teams = (request.getTeamsFilter() != null && !request.getTeamsFilter().isEmpty() && !request.getTeamsFilter().contains("All")) 
            ? request.getTeamsFilter() : null;
        List<Integer> ages = null;
        
        // Convert age strings to integers - optimized with parallelStream
        if (request.getAgeRange() != null && !request.getAgeRange().isEmpty() && !request.getAgeRange().contains("All")) {
            ages = request.getAgeRange().parallelStream()
                .filter(ageStr -> !ageStr.equals("All"))
                .map(ageStr -> {
                    try {
                        return Integer.parseInt(ageStr.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        
        sqlQueriesCount++;
        data = playerStatsRepository.findByYearWithFilters(
            year,
            request.getMinGamesPlayed(),
            request.getMinMinutesPerGame() != null ? request.getMinMinutesPerGame().doubleValue() : null,
            positions,
            teams,
            ages
        );
        
        totalRecordsProcessed += data.size();
        
        // Apply awards filter if specified - optimized with HashSet for O(1) lookup
        if (request.getAwards() != null && !request.getAwards().isEmpty() && !request.getAwards().contains("All")) {
            Set<String> requestedAwards = new HashSet<>(request.getAwards());
            data = data.parallelStream()
                .filter(stat -> {
                    if (stat.getAwards() == null || stat.getAwards().trim().isEmpty()) {
                        return false;
                    }
                    // Use HashSet intersection for faster award matching
                    String[] playerAwards = stat.getAwards().split(",");
                    for (String award : playerAwards) {
                        if (requestedAwards.contains(award.trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        // Optimized multi-team filtering - single pass with early termination
        if (data.size() > 1000) { // Only do complex filtering for large datasets
            Map<String, Boolean> playerYearMultiTeam = new HashMap<>();
            
            // Single pass to identify multi-team players
            for (PlayerStats stat : data) {
                if (stat.getTeam() != null && stat.getTeam().contains("TM")) {
                    String key = stat.getPlayer() + "_" + stat.getYear();
                    playerYearMultiTeam.put(key, true);
                }
            }
            
            // Filter in single pass
            data = data.parallelStream()
                .filter(stat -> {
                    if (stat.getTeam() == null) return true; // Single team player
                    if (stat.getTeam().contains("TM")) return true; // Multi-team overall
                    
                    // Check if this player has a multi-team record
                    String key = stat.getPlayer() + "_" + stat.getYear();
                    return !playerYearMultiTeam.containsKey(key);
                })
                .collect(Collectors.toList());
        }
        
        return data;
    }

    private Double getValueForAxis(PlayerStats stat, String axisType) {
        return getValueForStat(stat, axisType);
    }

    private Double getValueForStat(PlayerStats stat, String statName) {
        if (statName == null) return null;
        

        
        switch (statName.toLowerCase()) {
            case "age":
                return stat.getAge() != null ? stat.getAge().doubleValue() : null;
            case "year":
                return stat.getYear() != null ? stat.getYear().doubleValue() : null;
            case "points":
            case "ppg":
                return stat.getPoints();
            case "assists":
                return stat.getAssists();
            case "rebounds":
                return stat.getTotalRebounds();
            case "steals":
                return stat.getSteals();
            case "blocks":
                return stat.getBlocks();
            case "minutes_per_game":
            case "mpg":
                return stat.getMinutesPerGame();
            case "field_goal_percentage":
            case "fg%":
                return stat.getFieldGoalPercentage();
            case "three_point_percentage":
            case "3p%":
                return stat.getThreePointPercentage();
            case "free_throw_percentage":
            case "ft%":
                return stat.getFreeThrowPercentage();
            case "games_played":
                return stat.getGamesPlayed() != null ? stat.getGamesPlayed().doubleValue() : null;
            case "games_started":
                return stat.getGamesStarted() != null ? stat.getGamesStarted().doubleValue() : null;
            case "turnovers":
                return stat.getTurnovers();
            case "personal_fouls":
                return stat.getPersonalFouls();
            // Additional shooting stats
            case "field_goals_made":
                return stat.getFieldGoalsMade();
            case "field_goals_attempted":
                return stat.getFieldGoalsAttempted();
            case "two_pointers_made":
                return stat.getTwoPointersMade();
            case "two_pointers_attempted":
                return stat.getTwoPointersAttempted();
            case "two_point_percentage":
                return stat.getTwoPointPercentage();
            case "effective_field_goal_percentage":
                return stat.getEffectiveFieldGoalPercentage();
            case "three_pointers_made":
                return stat.getThreePointersMade();
            case "three_pointers_attempted":
                return stat.getThreePointersAttempted();
            case "free_throws_made":
                return stat.getFreeThrowsMade();
            case "free_throws_attempted":
                return stat.getFreeThrowsAttempted();
            // Additional rebounding stats
            case "offensive_rebounds":
                return stat.getOffensiveRebounds();
            case "defensive_rebounds":
                return stat.getDefensiveRebounds();

            default:
                return null;
        }
    }

    private String getAxisLabel(String axisType) {
        if (axisType == null) return "X-Axis";
        
        switch (axisType.toLowerCase()) {
            case "age":
                return "Age";
            case "year":
            case "season":
                return "Season";
            case "points":
            case "ppg":
                return "Points Per Game";
            case "assists":
                return "Assists Per Game";
            case "rebounds":
                return "Rebounds Per Game";
            case "steals":
                return "Steals Per Game";
            case "blocks":
                return "Blocks Per Game";
            case "minutes_per_game":
            case "mpg":
                return "Minutes Per Game";
            case "field_goal_percentage":
            case "fg%":
                return "Field Goal %";
            case "three_point_percentage":
            case "3p%":
                return "3-Point %";
            case "free_throw_percentage":
            case "ft%":
                return "Free Throw %";
            case "turnovers":
                return "Turnovers Per Game";
            case "personal_fouls":
                return "Personal Fouls Per Game";
            // Additional shooting stats
            case "field_goals_made":
                return "Field Goals Made Per Game";
            case "field_goals_attempted":
                return "Field Goals Attempted Per Game";
            case "two_pointers_made":
                return "2-Pointers Made Per Game";
            case "two_pointers_attempted":
                return "2-Pointers Attempted Per Game";
            case "two_point_percentage":
                return "2-Point %";
            case "effective_field_goal_percentage":
                return "Effective Field Goal %";
            case "three_pointers_made":
                return "3-Pointers Made Per Game";
            case "three_pointers_attempted":
                return "3-Pointers Attempted Per Game";
            case "free_throws_made":
                return "Free Throws Made Per Game";
            case "free_throws_attempted":
                return "Free Throws Attempted Per Game";
            // Additional rebounding stats
            case "offensive_rebounds":
                return "Offensive Rebounds Per Game";
            case "defensive_rebounds":
                return "Defensive Rebounds Per Game";

            default:
                return axisType;
        }
    }
    
    private String getStatLabel(String statName) {
        return getAxisLabel(statName);
    }
    
    private String getRandomColor() {
        String[] colors = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", 
            "#9966FF", "#FF9F40", "#FF6384", "#C9CBCF"
        };
        return colors[new Random().nextInt(colors.length)];
    }
    
    private String getConcentrationColor(double value, double min, double max) {
        // Create a color gradient from red (low concentration) to green (high concentration)
        double normalizedValue = (value - min) / (max - min);
        normalizedValue = Math.max(0, Math.min(1, normalizedValue)); // Clamp between 0 and 1
        
        // Red to green gradient
        int red = (int)(255 * (1 - normalizedValue)); // 255 (red) to 0
        int green = (int)(255 * normalizedValue);     // 0 to 255 (green)
        int blue = 0;                                 // No blue component
        
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    
    private String getColorForIndex(int index) {
        String[] colors = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", 
            "#9966FF", "#FF9F40", "#FF6384", "#C9CBCF"
        };
        return colors[index % colors.length];
    }




    public List<String> searchPlayers(String search) {
        if (search == null || search.trim().isEmpty()) {
            return playerStatsRepository.findAllPlayers();
        }
        return playerStatsRepository.searchPlayers(search.trim());
    }

    public List<String> getTeams() {
        return playerStatsRepository.findAllTeams();
    }



    public List<String> getAwards() {
        List<String> allAwards = playerStatsRepository.findFilteredAwards();
        
        // Filter awards to only include top 5 for MVP, DPOY, 6MOY, and exclude high place numbers
        List<String> filteredAwards = allAwards.stream()
            .filter(award -> {
                if (award == null || award.trim().isEmpty()) {
                    return false;
                }
                
                String trimmedAward = award.trim();
                
                // Handle MVP awards (MVP-1, MVP-2, etc.)
                if (trimmedAward.startsWith("MVP-")) {
                    try {
                        int place = Integer.parseInt(trimmedAward.substring(4));
                        return place <= 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle DPOY awards (DPOY-1, DPOY-2, etc.)
                if (trimmedAward.startsWith("DPOY-")) {
                    try {
                        int place = Integer.parseInt(trimmedAward.substring(5));
                        return place <= 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle 6MOY awards (6MOY-1, 6MOY-2, etc.)
                if (trimmedAward.startsWith("6MOY-")) {
                    try {
                        int place = Integer.parseInt(trimmedAward.substring(5));
                        return place <= 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle ROY awards (ROY-1, ROY-2, etc.) - ROY should be treated as ROY-1
                if (trimmedAward.startsWith("ROY-")) {
                    try {
                        int place = Integer.parseInt(trimmedAward.substring(4));
                        return place <= 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle NBA awards (NBA1, NBA2, NBA3) - no dash
                if (trimmedAward.startsWith("NBA")) {
                    try {
                        // Extract number from NBA1, NBA2, NBA3
                        String numberPart = trimmedAward.substring(3);
                        int place = Integer.parseInt(numberPart);
                        return place <= 3; // Only top 3 for NBA
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle DEF awards (DEF1, DEF2) - no dash
                if (trimmedAward.startsWith("DEF")) {
                    try {
                        // Extract number from DEF1, DEF2
                        String numberPart = trimmedAward.substring(3);
                        int place = Integer.parseInt(numberPart);
                        return place <= 2; // Only top 2 for DEF
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // Handle MIP awards (MIP-1 only)
                if (trimmedAward.startsWith("MIP-")) {
                    try {
                        int place = Integer.parseInt(trimmedAward.substring(4));
                        return place == 1; // Only MIP-1
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                
                // For other awards with place numbers (e.g., "All-NBA-1", "All-Defense-2")
                if (trimmedAward.contains("-")) {
                    String[] parts = trimmedAward.split("-");
                    if (parts.length > 1) {
                        try {
                            int place = Integer.parseInt(parts[parts.length - 1]);
                            return place <= 5;
                        } catch (NumberFormatException e) {
                            // If the last part isn't a number, include the award
                            return true;
                        }
                    }
                }
                
                // Include all other awards (All-Star, etc.)
                return true;
            })
            .collect(Collectors.toList());
        
        // Custom sorting: MVP, DPOY, NBA 1-3, DEF 1-2, ROY, 6MOY, MIP, then others
        return filteredAwards.stream()
            .sorted((a, b) -> {
                int orderA = getAwardOrder(a);
                int orderB = getAwardOrder(b);
                
                if (orderA != orderB) {
                    return Integer.compare(orderA, orderB);
                }
                
                // If same category, sort alphabetically
                return a.compareTo(b);
            })
            .collect(Collectors.toList());
    }
    
    private int getAwardOrder(String award) {
        String trimmedAward = award.trim();
        
        // MVP awards (MVP-1, MVP-2, etc.)
        if (trimmedAward.startsWith("MVP-")) {
            return 1;
        }

        // All stars
        if (trimmedAward.startsWith("AS")) {
            return 2;
        }

        // DPOY awards (DPOY-1, DPOY-2, etc.)
        if (trimmedAward.startsWith("DPOY-")) {
            return 3;
        }
        
        // All-NBA awards (NBA1, NBA2, NBA3)
        if (trimmedAward.startsWith("NBA")) {
            return 4;
        }
        
        // All-Defense awards (DEF1, DEF2)
        if (trimmedAward.startsWith("DEF")) {
            return 5;
        }
        
        // ROY awards (ROY-1, ROY-2, etc.)
        if (trimmedAward.startsWith("ROY-")) {
            return 6;
        }
        
        // 6MOY awards (6MOY-1, 6MOY-2, etc.)
        if (trimmedAward.startsWith("6MOY-")) {
            return 7;
        }
        
        // MIP awards (MIP-1, MIP-2, etc.)
        if (trimmedAward.startsWith("MIP-")) {
            return 8;
        }
        
        // All other awards come last
        return 999;
    }
}
