package com.nba.service;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;

import com.nba.entity.PlayerStats;
import com.nba.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {
    
    @Autowired
    private PlayerStatsRepository playerStatsRepository;


    
    public GraphResponse generateGraph(GraphRequest request) {
        GraphResponse response = new GraphResponse();
        response.setGraphType(request.getGraphType());
        
        // Debug: Print all request fields
        System.out.println("=== REQUEST DEBUG ===");
        System.out.println("graphType: " + request.getGraphType());
        System.out.println("template: " + request.getTemplate());
        System.out.println("xAxisType: " + request.getXAxisType());
        System.out.println("yAxisType: " + request.getYAxisType());
        System.out.println("xAxisStat: " + request.getXAxisStat());
        System.out.println("yAxisStat: " + request.getYAxisStat());
        System.out.println("stat: " + request.getStat());
        System.out.println("year: " + request.getYear());
        System.out.println("years: " + request.getYears());
        System.out.println("players: " + request.getPlayers());
        System.out.println("====================");
        
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
                
                System.out.println("Scatter plot - Original xAxisStat: " + xAxisStat);
                System.out.println("Scatter plot - Original yAxisStat: " + yAxisStat);
                
                if (xAxisStat == null) {
                    xAxisStat = "steals";
                    request.setXAxisStat(xAxisStat);
                    System.out.println("Scatter plot - Using default xAxisStat: " + xAxisStat);
                }
                if (yAxisStat == null) {
                    yAxisStat = "blocks";
                    request.setYAxisStat(yAxisStat);
                    System.out.println("Scatter plot - Using default yAxisStat: " + yAxisStat);
                }
                
                System.out.println("Scatter plot - Final xAxisStat: " + xAxisStat);
                System.out.println("Scatter plot - Final yAxisStat: " + yAxisStat);
                break;
        }

        switch (request.getGraphType()) {
            case "line":
                return generateLineGraph(request, response);
            case "histogram":
                return generateHistogram(request, response);
            case "scatter":
                return generateScatterPlot(request, response);
            default:
                throw new IllegalArgumentException("Unsupported graph type: " + request.getGraphType());
        }
    }

    private GraphResponse generateLineGraph(GraphRequest request, GraphResponse response) {
        List<GraphResponse.LineDataset> datasets = new ArrayList<>();
        
        System.out.println("Generating line graph for players: " + request.getPlayers());
        System.out.println("X-Axis: " + request.getXAxisType() + ", Y-Axis: " + request.getYAxisType());
        
        if (request.getPlayers() != null) {
            for (int i = 0; i < request.getPlayers().size(); i++) {
                String player = request.getPlayers().get(i);
                List<PlayerStats> playerData = getPlayerData(player, request);
                System.out.println("Processing " + playerData.size() + " records for " + player);
                
                List<Double> xValues = new ArrayList<>();
                List<Double> yValues = new ArrayList<>();
                
                for (PlayerStats stat : playerData) {
                    Double xValue = getValueForAxis(stat, request.getXAxisType());
                    Double yValue = getValueForAxis(stat, request.getYAxisType());
                    
                    System.out.println("  Record: Year=" + stat.getYear() + ", Age=" + stat.getAge() + ", Points=" + stat.getPoints() + 
                                     " -> X=" + xValue + ", Y=" + yValue);
                    
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
                
                System.out.println("Final X values: " + sortedXValues);
                System.out.println("Final Y values: " + sortedYValues);
                
                if (!sortedXValues.isEmpty()) {
                    GraphResponse.LineDataset dataset = new GraphResponse.LineDataset();
                    dataset.setLabel(player);
                    dataset.setXValues(sortedXValues);
                    dataset.setYValues(sortedYValues);
                    dataset.setBorderColor(getColorForIndex(i));
                    dataset.setFill(false);
                    datasets.add(dataset);
                    System.out.println("Added dataset for " + player + " with " + sortedXValues.size() + " points");
                } else {
                    System.out.println("No valid data points for " + player);
                }
            }
        }
        
        System.out.println("Total datasets created: " + datasets.size());
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
        
        System.out.println("Histogram: Found " + data.size() + " records for stat " + request.getStat());
        
        // Debug: Show some examples of the data being processed
        System.out.println("Histogram: Sample data being processed:");
        data.stream()
            .limit(10)
            .forEach(stat -> {
                Double value = getValueForStat(stat, request.getStat());
                System.out.println("  " + stat.getPlayer() + " (" + stat.getYear() + ") - " + stat.getTeam() + " = " + value);
            });
        
        // Calculate histogram bins
        List<Double> values = data.stream()
            .map(stat -> getValueForStat(stat, request.getStat()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        System.out.println("Histogram: Valid values: " + values.size());
        
        if (values.isEmpty()) {
            response.setBinEdges(new ArrayList<>());
            response.setBinCounts(new ArrayList<>());
            return response;
        }
        
        int binCount = request.getBinCount() != null ? request.getBinCount() : 20;
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double binWidth = (max - min) / binCount;
        
        System.out.println("Histogram binning: min=" + min + ", max=" + max + ", binWidth=" + binWidth + ", binCount=" + binCount);
        
        List<Double> binEdges = new ArrayList<>();
        List<Integer> binCounts = new ArrayList<>();
        List<List<String>> binPlayers = new ArrayList<>(); // Store players in each bin
        
        for (int i = 0; i <= binCount; i++) {
            binEdges.add(min + i * binWidth);
        }
        
        for (int i = 0; i < binCount; i++) {
            final double lowerBound = binEdges.get(i);
            final double upperBound = binEdges.get(i + 1);
            
            List<PlayerStats> binData;
            if (i == binCount - 1) {
                // For the last bin, include the upper bound (inclusive)
                binData = data.stream()
                    .filter(stat -> {
                        Double value = getValueForStat(stat, request.getStat());
                        return value != null && value >= lowerBound && value <= upperBound;
                    })
                    .collect(Collectors.toList());
            } else {
                // For all other bins, exclude the upper bound
                binData = data.stream()
                    .filter(stat -> {
                        Double value = getValueForStat(stat, request.getStat());
                        return value != null && value >= lowerBound && value < upperBound;
                    })
                    .collect(Collectors.toList());
            }
            
            int count = binData.size();
            binCounts.add(count);
            
            // Store all player names with their values
            List<String> playerNames = binData.stream()
                .map(stat -> {
                    Double value = getValueForStat(stat, request.getStat());
                    return stat.getPlayer() + " (" + stat.getYear() + ") - " + String.format("%.1f", value);
                })
                .collect(Collectors.toList());
            binPlayers.add(playerNames);
            
            System.out.println("Bin " + i + ": [" + lowerBound + ", " + upperBound + (i == binCount - 1 ? "]" : ")") + " -> " + count + " values");
        }
        
        System.out.println("Total values in bins: " + binCounts.stream().mapToInt(Integer::intValue).sum());
        System.out.println("Original values count: " + values.size());
        
        response.setBinEdges(binEdges);
        response.setBinCounts(binCounts);
        
        // Add metadata for frontend axis labels and tooltips
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("binPlayers", binPlayers);
        metadata.put("stat", request.getStat());
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
        
        System.out.println("Scatter plot: Found " + data.size() + " records");
        
        // Use the values from the request (fallbacks already applied in generateGraph)
        String xAxisStat = request.getXAxisStat();
        String yAxisStat = request.getYAxisStat();
        
        System.out.println("Scatter plot using xAxisStat: " + xAxisStat + ", yAxisStat: " + yAxisStat);
        
        List<GraphResponse.ScatterPoint> points = new ArrayList<>();
        
        for (PlayerStats stat : data) {
            Double xValue = getValueForStat(stat, xAxisStat);
            Double yValue = getValueForStat(stat, yAxisStat);
            
            System.out.println("Player: " + stat.getPlayer() + ", X: " + xValue + ", Y: " + yValue);
            
            if (xValue != null && yValue != null) {
                GraphResponse.ScatterPoint point = new GraphResponse.ScatterPoint();
                point.setX(xValue);
                point.setY(yValue);
                point.setPlayer(stat.getPlayer());
                point.setTeam(stat.getTeam());
                point.setYear(stat.getYear());
                point.setLabel(stat.getPlayer());
                point.setColor("#D3D3D3"); // Light gray color for all points
                points.add(point);
            }
        }
        
        System.out.println("Scatter plot: Created " + points.size() + " points");
        response.setPoints(points);
        
        // Add metadata for frontend axis labels
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("xAxisStat", xAxisStat);
        metadata.put("yAxisStat", yAxisStat);
        response.setMetadata(metadata);
        
        return response;
    }

    private List<PlayerStats> getPlayerData(String playerName, GraphRequest request) {
        System.out.println("Searching for player: '" + playerName + "'");
        List<PlayerStats> data = playerStatsRepository.findPlayerAllStats(playerName);
        System.out.println("Found " + data.size() + " records for player: " + playerName);
        
        if (data.isEmpty()) {
            // Try a case-insensitive search
            List<String> allPlayers = playerStatsRepository.findAllPlayers();
            System.out.println("Available players containing '" + playerName + "':");
            allPlayers.stream()
                .filter(p -> p.toLowerCase().contains(playerName.toLowerCase()))
                .limit(5)
                .forEach(p -> System.out.println("  - " + p));
        }
        
        // Apply games played filter if specified
        if (request.getMinGamesPlayed() != null && request.getMinGamesPlayed() > 0) {
            data = data.stream()
                .filter(stat -> stat.getGamesPlayed() != null && stat.getGamesPlayed() >= request.getMinGamesPlayed())
                .collect(Collectors.toList());
            System.out.println("getPlayerData: After filtering by games, " + data.size() + " records remain");
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
            System.out.println("getPlayerData: After filtering by minutes, " + data.size() + " records remain");
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
        
        if (request.getMinGamesPlayed() != null && request.getMinGamesPlayed() > 0) {
            data = playerStatsRepository.findByYearAndMinGames(year, request.getMinGamesPlayed());
        } else {
            data = playerStatsRepository.findByYear(year);
        }
        
        System.out.println("getSeasonData: Found " + data.size() + " records for year " + year);
        
        // Debug: Check for early years data quality
        if (year <= 1951) {
            System.out.println("=== DEBUG: Early year " + year + " data ===");
            data.stream().limit(5).forEach(stat -> {
                System.out.println("Player: " + stat.getPlayer() + 
                    ", Team: " + stat.getTeam() + 
                    ", Position: " + stat.getPosition() + 
                    ", Age: " + stat.getAge() +
                    ", Points: " + stat.getPoints() +
                    ", Games: " + stat.getGamesPlayed());
            });
            System.out.println("=== END DEBUG ===");
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
            System.out.println("getSeasonData: After filtering by minutes, " + data.size() + " records remain");
        }
        
        // Apply position filter if specified
        if (request.getPositions() != null && !request.getPositions().isEmpty() && !request.getPositions().contains("All")) {
            data = data.stream()
                .filter(stat -> stat.getPosition() != null && request.getPositions().contains(stat.getPosition()))
                .collect(Collectors.toList());
            System.out.println("getSeasonData: After filtering by positions, " + data.size() + " records remain");
        }
        
        // Apply awards filter if specified
        if (request.getAwards() != null && !request.getAwards().isEmpty() && !request.getAwards().contains("All")) {
            data = data.stream()
                .filter(stat -> {
                    if (stat.getAwards() == null || stat.getAwards().trim().isEmpty()) {
                        return false;
                    }
                    String[] playerAwards = stat.getAwards().split(",");
                    return Arrays.stream(playerAwards)
                        .anyMatch(award -> request.getAwards().contains(award.trim()));
                })
                .collect(Collectors.toList());
            System.out.println("getSeasonData: After filtering by awards, " + data.size() + " records remain");
        }
        
        // Apply teams filter if specified
        if (request.getTeamsFilter() != null && !request.getTeamsFilter().isEmpty() && !request.getTeamsFilter().contains("All")) {
            data = data.stream()
                .filter(stat -> {
                    if (stat.getTeam() == null || stat.getTeam().trim().isEmpty()) {
                        return false;
                    }
                    return request.getTeamsFilter().contains(stat.getTeam().trim());
                })
                .collect(Collectors.toList());
            System.out.println("getSeasonData: After filtering by teams, " + data.size() + " records remain");
        }
        
        // Apply age filter if specified
        if (request.getAgeRange() != null && !request.getAgeRange().isEmpty() && !request.getAgeRange().contains("All")) {
            data = data.stream()
                .filter(stat -> {
                    if (stat.getAge() == null) {
                        return false;
                    }
                    return request.getAgeRange().stream().anyMatch(ageStr -> {
                        if (ageStr.equals("All")) return true;
                        try {
                            int selectedAge = Integer.parseInt(ageStr.trim());
                            return stat.getAge() == selectedAge;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    });
                })
                .collect(Collectors.toList());
            System.out.println("getSeasonData: After filtering by age, " + data.size() + " records remain");
        }
        
        // Debug: Show some examples of multi-team records
        System.out.println("getSeasonData: Sample records before filtering:");
        data.stream()
            .filter(stat -> stat.getTeam() != null && stat.getTeam().contains("TM"))
            .limit(5)
            .forEach(stat -> System.out.println("  Multi-team: " + stat.getPlayer() + " (" + stat.getYear() + ") - " + stat.getTeam()));
        
        data.stream()
            .filter(stat -> stat.getTeam() != null && !stat.getTeam().contains("TM"))
            .limit(5)
            .forEach(stat -> System.out.println("  Individual team: " + stat.getPlayer() + " (" + stat.getYear() + ") - " + stat.getTeam()));
        
        // For histograms and scatter plots, use multi-team overall stats (filter out individual team records)
        // This ensures we don't double-count players who played for multiple teams
        
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
        
        System.out.println("getSeasonData: After filtering multi-team, " + data.size() + " records remain");
        
        return data;
    }

    private Double getValueForAxis(PlayerStats stat, String axisType) {
        return getValueForStat(stat, axisType);
    }

    private Double getValueForStat(PlayerStats stat, String statName) {
        if (statName == null) return null;
        
        System.out.println("Getting value for stat: '" + statName + "' for player: " + stat.getPlayer());
        
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
            default:
                System.out.println("Unknown stat: " + statName);
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
        
        // ROY (Rookie of the Year)
        if (trimmedAward.equals("ROY")) {
            return 6;
        }
        
        // 6MOY awards (6MOY-1, 6MOY-2, etc.)
        if (trimmedAward.startsWith("6MOY-")) {
            return 7;
        }
        
        // MIP (Most Improved Player)
        if (trimmedAward.equals("MIP")) {
            return 8;
        }
        
        // All other awards come last
        return 999;
    }
}
