package com.nba.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class GraphResponse {
    private String graphType;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    
    // For line graphs
    private List<LineDataset> datasets;
    
    // For histograms
    private List<Double> binEdges;
    private List<Integer> binCounts;
    
    // For scatter plots
    private List<ScatterPoint> points;
    
    // Metadata
    private Map<String, Object> metadata;
    private String sqlQuery; // For debugging
    
    // Constructors
    public GraphResponse() {}
    
    // Inner classes for different graph types
    public static class LineDataset {
        private String label;
        @JsonProperty("xValues")
        private List<Double> xValues;
        @JsonProperty("yValues")
        private List<Double> yValues;
        private String borderColor;
        private String backgroundColor;
        private boolean fill;
        
        public LineDataset() {}
        
        public LineDataset(String label, List<Double> xValues, List<Double> yValues) {
            this.label = label;
            this.xValues = xValues;
            this.yValues = yValues;
        }
        
        // Getters and Setters
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public List<Double> getXValues() {
            return xValues;
        }
        
        public void setXValues(List<Double> xValues) {
            this.xValues = xValues;
        }
        
        public List<Double> getYValues() {
            return yValues;
        }
        
        public void setYValues(List<Double> yValues) {
            this.yValues = yValues;
        }
        
        public String getBorderColor() {
            return borderColor;
        }
        
        public void setBorderColor(String borderColor) {
            this.borderColor = borderColor;
        }
        
        public String getBackgroundColor() {
            return backgroundColor;
        }
        
        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
        
        public boolean isFill() {
            return fill;
        }
        
        public void setFill(boolean fill) {
            this.fill = fill;
        }
    }
    
    public static class ScatterPoint {
        private Double x;
        private Double y;
        private String player;
        private String team;
        private String position;
        private Integer year;
        private String label;
        private String color;
        
        public ScatterPoint() {}
        
        public ScatterPoint(Double x, Double y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
        
        public ScatterPoint(Double x, Double y, String player, String team, Integer year) {
            this.x = x;
            this.y = y;
            this.player = player;
            this.team = team;
            this.year = year;
            this.label = player;
        }
        
        // Getters and Setters
        public Double getX() {
            return x;
        }
        
        public void setX(Double x) {
            this.x = x;
        }
        
        public Double getY() {
            return y;
        }
        
        public void setY(Double y) {
            this.y = y;
        }
        
        public String getPlayer() {
            return player;
        }
        
        public void setPlayer(String player) {
            this.player = player;
        }
        
        public String getTeam() {
            return team;
        }
        
        public void setTeam(String team) {
            this.team = team;
        }
        
        public String getPosition() {
            return position;
        }
        
        public void setPosition(String position) {
            this.position = position;
        }
        
        public Integer getYear() {
            return year;
        }
        
        public void setYear(Integer year) {
            this.year = year;
        }
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
    }
    
    // Getters and Setters
    public String getGraphType() {
        return graphType;
    }
    
    public void setGraphType(String graphType) {
        this.graphType = graphType;
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
    
    public List<LineDataset> getDatasets() {
        return datasets;
    }
    
    public void setDatasets(List<LineDataset> datasets) {
        this.datasets = datasets;
    }
    
    public List<Double> getBinEdges() {
        return binEdges;
    }
    
    public void setBinEdges(List<Double> binEdges) {
        this.binEdges = binEdges;
    }
    
    public List<Integer> getBinCounts() {
        return binCounts;
    }
    
    public void setBinCounts(List<Integer> binCounts) {
        this.binCounts = binCounts;
    }
    
    public List<ScatterPoint> getPoints() {
        return points;
    }
    
    public void setPoints(List<ScatterPoint> points) {
        this.points = points;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getSqlQuery() {
        return sqlQuery;
    }
    
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
}

