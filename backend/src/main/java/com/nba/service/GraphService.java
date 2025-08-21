package com.nba.service;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;
import com.nba.dto.TemplateInfo;
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

    // Available statistics for selection
    private static final List<String> AVAILABLE_STATS = Arrays.asList(
        "points", "assists", "rebounds", "steals", "blocks", "turnovers", 
        "field_goal_percentage", "three_point_percentage", "free_throw_percentage",
        "minutes_per_game", "games_played", "age"
    );

    // Template definitions
    private static final Map<String, TemplateInfo> TEMPLATES = new HashMap<>();

    static {
        // Player Comparison Line Graph Template
        TemplateInfo playerComparison = new TemplateInfo(
            "player_comparison", 
            "Player Comparison", 
            "Compare multiple players' statistics over their careers", 
            "line"
        );
        
        List<TemplateInfo.FieldInfo> playerFields = Arrays.asList(
            new TemplateInfo.FieldInfo("players", "Players", "multiselect", "LeBron James,Stephen Curry"),
            new TemplateInfo.FieldInfo("xAxisType", "X-Axis", "select", "age"),
            new TemplateInfo.FieldInfo("yAxisType", "Y-Axis", "select", "points"),
            new TemplateInfo.FieldInfo("minGamesPlayed", "Minimum Games Played", "number", "10"),
            new TemplateInfo.FieldInfo("minMinutesPerGame", "Minimum Minutes Per Game", "number", "10")
        );
        playerComparison.setFields(playerFields);
        TEMPLATES.put("player_comparison", playerComparison);

        // Season Distribution Histogram Template
        TemplateInfo seasonDistribution = new TemplateInfo(
            "season_distribution", 
            "Season Stat Distribution", 
            "Show the distribution of a statistic for all players in a season", 
            "histogram"
        );
        
        List<TemplateInfo.FieldInfo> histogramFields = Arrays.asList(
            new TemplateInfo.FieldInfo("years", "Seasons", "multiselect", "1996,1997,1998"),
            new TemplateInfo.FieldInfo("stat", "Statistic", "select", "points"),
            new TemplateInfo.FieldInfo("binCount", "Number of Bins", "number", "20"),
            new TemplateInfo.FieldInfo("minGamesPlayed", "Minimum Games Played", "number", "10"),
            new TemplateInfo.FieldInfo("minMinutesPerGame", "Minimum Minutes Per Game", "number", "10")
        );
        seasonDistribution.setFields(histogramFields);
        TEMPLATES.put("season_distribution", seasonDistribution);

        // Season Correlation Scatter Plot Template
        TemplateInfo seasonCorrelation = new TemplateInfo(
            "season_correlation", 
            "Season Correlation", 
            "Show correlation between two statistics for all players in a season", 
            "scatter"
        );
        
        List<TemplateInfo.FieldInfo> scatterFields = Arrays.asList(
            new TemplateInfo.FieldInfo("years", "Seasons", "multiselect", "2013,2014,2015"),
            new TemplateInfo.FieldInfo("xAxisStat", "X-Axis Statistic", "select", "steals"),
            new TemplateInfo.FieldInfo("yAxisStat", "Y-Axis Statistic", "select", "blocks"),
            new TemplateInfo.FieldInfo("minGamesPlayed", "Minimum Games Played", "number", "10"),
            new TemplateInfo.FieldInfo("minMinutesPerGame", "Minimum Minutes Per Game", "number", "10")
        );
        seasonCorrelation.setFields(scatterFields);
        TEMPLATES.put("season_correlation", seasonCorrelation);
    }
    
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
        
        // Set dynamic labels based on graph type and selected stats
        String title = request.getTitle();
        String xAxisLabel = request.getXAxisLabel();
        String yAxisLabel = request.getYAxisLabel();
        
        switch (request.getGraphType()) {
            case "line":
                // Use default values if not provided
                String xAxisType = request.getXAxisType();
                String yAxisType = request.getYAxisType();
                
                if (xAxisType == null) {
                    xAxisType = "age";
                    request.setXAxisType(xAxisType); // Update the request object
                }
                if (yAxisType == null) {
                    yAxisType = "points";
                    request.setYAxisType(yAxisType); // Update the request object
                }
                
                if (title == null) {
                    title = "Player Comparison";
                }
                if (xAxisLabel == null) {
                    xAxisLabel = getAxisLabel(xAxisType);
                }
                if (yAxisLabel == null) {
                    yAxisLabel = getAxisLabel(yAxisType);
                }
                break;
                         case "histogram":
                 // Use default values if not provided
                 String stat = request.getStat();
                 if (stat == null) {
                     stat = "points";
                     request.setStat(stat); // Update the request object
                 }
                 
                 if (title == null) {
                     if (request.getYears() != null && !request.getYears().isEmpty()) {
                         String yearsStr = request.getYears().stream()
                             .map(String::valueOf)
                             .collect(Collectors.joining(", "));
                         title = yearsStr + " " + getStatLabel(stat) + " Distribution";
                     } else if (request.getYear() != null) {
                         title = request.getYear() + " " + getStatLabel(stat) + " Distribution";
                     } else {
                         title = getStatLabel(stat) + " Distribution";
                     }
                 }
                 if (xAxisLabel == null) {
                     xAxisLabel = getStatLabel(stat);
                 }
                 if (yAxisLabel == null) {
                     yAxisLabel = "Players";
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
                    request.setXAxisStat(xAxisStat); // Update the request object
                    System.out.println("Scatter plot - Using default xAxisStat: " + xAxisStat);
                }
                if (yAxisStat == null) {
                    yAxisStat = "blocks";
                    request.setYAxisStat(yAxisStat); // Update the request object
                    System.out.println("Scatter plot - Using default yAxisStat: " + yAxisStat);
                }
                
                System.out.println("Scatter plot - Final xAxisStat: " + xAxisStat);
                System.out.println("Scatter plot - Final yAxisStat: " + yAxisStat);
                
                if (title == null) {
                    title = getStatLabel(xAxisStat) + " vs " + getStatLabel(yAxisStat);
                }
                if (xAxisLabel == null) {
                    xAxisLabel = getStatLabel(xAxisStat);
                }
                if (yAxisLabel == null) {
                    yAxisLabel = getStatLabel(yAxisStat);
                }
                break;
        }
        
        response.setTitle(title);
        response.setXAxisLabel(xAxisLabel);
        response.setYAxisLabel(yAxisLabel);

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
        
        // Add player names to metadata for tooltips
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("binPlayers", binPlayers);
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
                point.setColor(getRandomColor());
                points.add(point);
            }
        }
        
        System.out.println("Scatter plot: Created " + points.size() + " points");
        response.setPoints(points);
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
        if (request.getMinMinutesPerGame() != null && request.getMinMinutesPerGame() > 0) {
            data = data.stream()
                .filter(stat -> stat.getMinutesPerGame() != null && stat.getMinutesPerGame() >= request.getMinMinutesPerGame())
                .collect(Collectors.toList());
            System.out.println("getPlayerData: After filtering by minutes, " + data.size() + " records remain");
        }
        
        // For line graphs, always use multi-team overall stats (filter out individual team records)
        data = data.stream()
            .filter(stat -> stat.getTeam() == null || !stat.getTeam().contains("TM"))
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
        
        // Apply minutes per game filter if specified
        if (request.getMinMinutesPerGame() != null && request.getMinMinutesPerGame() > 0) {
            data = data.stream()
                .filter(stat -> stat.getMinutesPerGame() != null && stat.getMinutesPerGame() >= request.getMinMinutesPerGame())
                .collect(Collectors.toList());
            System.out.println("getSeasonData: After filtering by minutes, " + data.size() + " records remain");
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
    
    private String getColorForIndex(int index) {
        String[] colors = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", 
            "#9966FF", "#FF9F40", "#FF6384", "#C9CBCF"
        };
        return colors[index % colors.length];
    }

    public List<TemplateInfo> getAvailableTemplates() {
        return new ArrayList<>(TEMPLATES.values());
    }

    public TemplateInfo getTemplate(String templateId) {
        return TEMPLATES.get(templateId);
    }

    public List<String> getAvailableStats() {
        return new ArrayList<>(AVAILABLE_STATS);
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

    public List<Integer> getYears() {
        return playerStatsRepository.findAllYears();
    }
}
