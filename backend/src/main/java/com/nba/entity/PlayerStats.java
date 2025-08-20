package com.nba.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_stats")
public class PlayerStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    @Column(name = "season")
    private String season;
    
    @Column(name = "points")
    private Double points;
    
    @Column(name = "rebounds")
    private Double rebounds;
    
    @Column(name = "assists")
    private Double assists;
    
    @Column(name = "steals")
    private Double steals;
    
    @Column(name = "blocks")
    private Double blocks;
    
    @Column(name = "field_goal_pct")
    private Double fieldGoalPct;
    
    @Column(name = "three_pt_pct")
    private Double threePtPct;
    
    @Column(name = "turnovers")
    private Double turnovers;
    
    // Constructors
    public PlayerStats() {}
    
    public PlayerStats(Player player, String season, Double points, Double rebounds, 
                      Double assists, Double steals, Double blocks, Double fieldGoalPct, 
                      Double threePtPct, Double turnovers) {
        this.player = player;
        this.season = season;
        this.points = points;
        this.rebounds = rebounds;
        this.assists = assists;
        this.steals = steals;
        this.blocks = blocks;
        this.fieldGoalPct = fieldGoalPct;
        this.threePtPct = threePtPct;
        this.turnovers = turnovers;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public String getSeason() {
        return season;
    }
    
    public void setSeason(String season) {
        this.season = season;
    }
    
    public Double getPoints() {
        return points;
    }
    
    public void setPoints(Double points) {
        this.points = points;
    }
    
    public Double getRebounds() {
        return rebounds;
    }
    
    public void setRebounds(Double rebounds) {
        this.rebounds = rebounds;
    }
    
    public Double getAssists() {
        return assists;
    }
    
    public void setAssists(Double assists) {
        this.assists = assists;
    }
    
    public Double getSteals() {
        return steals;
    }
    
    public void setSteals(Double steals) {
        this.steals = steals;
    }
    
    public Double getBlocks() {
        return blocks;
    }
    
    public void setBlocks(Double blocks) {
        this.blocks = blocks;
    }
    
    public Double getFieldGoalPct() {
        return fieldGoalPct;
    }
    
    public void setFieldGoalPct(Double fieldGoalPct) {
        this.fieldGoalPct = fieldGoalPct;
    }
    
    public Double getThreePtPct() {
        return threePtPct;
    }
    
    public void setThreePtPct(Double threePtPct) {
        this.threePtPct = threePtPct;
    }
    
    public Double getTurnovers() {
        return turnovers;
    }
    
    public void setTurnovers(Double turnovers) {
        this.turnovers = turnovers;
    }
}
