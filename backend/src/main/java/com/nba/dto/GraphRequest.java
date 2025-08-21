package com.nba.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GraphRequest {
    private String graphType; // "line", "histogram", "scatter"
    private String template; // "player_comparison", "season_distribution", "season_correlation", "custom"
    
    // Common fields
    private String xAxis;
    private String yAxis;
    private Integer year;
    private List<Integer> years;
    private String stat;
    
    // Line graph specific
    private List<String> players;
    @JsonProperty("xAxisType")
    private String xAxisType; // "age", "year", etc.
    @JsonProperty("yAxisType")
    private String yAxisType; // "ppg", "assists", etc.
    
    // Histogram specific
    private Integer binCount;
    private Double minValue;
    private Double maxValue;
    
    // Scatter plot specific
    @JsonProperty("xAxisStat")
    private String xAxisStat;
    @JsonProperty("yAxisStat")
    private String yAxisStat;
    
    // Filtering options
    private Boolean includeMultiTeamPlayers;
    private Integer minGamesPlayed;
    private Integer minMinutesPerGame;
    private List<String> positions;
    private List<String> teams;
    
    // General customization
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    
    // Constructors
    public GraphRequest() {}
    
    // Getters and Setters
    public String getGraphType() {
        return graphType;
    }
    
    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getXAxis() {
        return xAxis;
    }
    
    public void setXAxis(String xAxis) {
        this.xAxis = xAxis;
    }
    
    public String getYAxis() {
        return yAxis;
    }
    
    public void setYAxis(String yAxis) {
        this.yAxis = yAxis;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public List<Integer> getYears() {
        return years;
    }
    
    public void setYears(List<Integer> years) {
        this.years = years;
    }
    
    public String getStat() {
        return stat;
    }
    
    public void setStat(String stat) {
        this.stat = stat;
    }
    
    public List<String> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<String> players) {
        this.players = players;
    }
    
    public String getXAxisType() {
        return xAxisType;
    }
    
    public void setXAxisType(String xAxisType) {
        this.xAxisType = xAxisType;
    }
    
    public String getYAxisType() {
        return yAxisType;
    }
    
    public void setYAxisType(String yAxisType) {
        this.yAxisType = yAxisType;
    }
    
    public Integer getBinCount() {
        return binCount;
    }
    
    public void setBinCount(Integer binCount) {
        this.binCount = binCount;
    }
    
    public Double getMinValue() {
        return minValue;
    }
    
    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }
    
    public Double getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }
    
    public String getXAxisStat() {
        return xAxisStat;
    }
    
    public void setXAxisStat(String xAxisStat) {
        this.xAxisStat = xAxisStat;
    }
    
    public String getYAxisStat() {
        return yAxisStat;
    }
    
    public void setYAxisStat(String yAxisStat) {
        this.yAxisStat = yAxisStat;
    }
    
    public Boolean getIncludeMultiTeamPlayers() {
        return includeMultiTeamPlayers;
    }
    
    public void setIncludeMultiTeamPlayers(Boolean includeMultiTeamPlayers) {
        this.includeMultiTeamPlayers = includeMultiTeamPlayers;
    }
    
    public Integer getMinGamesPlayed() {
        return minGamesPlayed;
    }
    
    public void setMinGamesPlayed(Integer minGamesPlayed) {
        this.minGamesPlayed = minGamesPlayed;
    }
    
    public Integer getMinMinutesPerGame() {
        return minMinutesPerGame;
    }
    
    public void setMinMinutesPerGame(Integer minMinutesPerGame) {
        this.minMinutesPerGame = minMinutesPerGame;
    }
    
    public List<String> getPositions() {
        return positions;
    }
    
    public void setPositions(List<String> positions) {
        this.positions = positions;
    }
    
    public List<String> getTeams() {
        return teams;
    }
    
    public void setTeams(List<String> teams) {
        this.teams = teams;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getXAxisLabel() {
        return xAxisLabel;
    }
    
    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }
    
    public String getYAxisLabel() {
        return yAxisLabel;
    }
    
    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }
}

