package com.nba.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nba_stats")
public class PlayerStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "player", nullable = false)
    private String player;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "team")
    private String team;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "games_played")
    private Integer gamesPlayed;
    
    @Column(name = "games_started")
    private Integer gamesStarted;
    
    @Column(name = "minutes_per_game")
    private Double minutesPerGame;
    
    @Column(name = "field_goals_made")
    private Double fieldGoalsMade;
    
    @Column(name = "field_goals_attempted")
    private Double fieldGoalsAttempted;
    
    @Column(name = "field_goal_percentage")
    private Double fieldGoalPercentage;
    
    @Column(name = "three_pointers_made")
    private Double threePointersMade;
    
    @Column(name = "three_pointers_attempted")
    private Double threePointersAttempted;
    
    @Column(name = "three_point_percentage")
    private Double threePointPercentage;
    
    @Column(name = "two_pointers_made")
    private Double twoPointersMade;
    
    @Column(name = "two_pointers_attempted")
    private Double twoPointersAttempted;
    
    @Column(name = "two_point_percentage")
    private Double twoPointPercentage;
    
    @Column(name = "effective_field_goal_percentage")
    private Double effectiveFieldGoalPercentage;
    
    @Column(name = "free_throws_made")
    private Double freeThrowsMade;
    
    @Column(name = "free_throws_attempted")
    private Double freeThrowsAttempted;
    
    @Column(name = "free_throw_percentage")
    private Double freeThrowPercentage;
    
    @Column(name = "offensive_rebounds")
    private Double offensiveRebounds;
    
    @Column(name = "defensive_rebounds")
    private Double defensiveRebounds;
    
    @Column(name = "total_rebounds")
    private Double totalRebounds;
    
    @Column(name = "assists")
    private Double assists;
    
    @Column(name = "steals")
    private Double steals;
    
    @Column(name = "blocks")
    private Double blocks;
    
    @Column(name = "turnovers")
    private Double turnovers;
    
    @Column(name = "personal_fouls")
    private Double personalFouls;
    
    @Column(name = "points")
    private Double points;
    
    @Column(name = "awards")
    private String awards;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public PlayerStats() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public void setPlayer(String player) {
        this.player = player;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
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
    
    public Integer getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public Integer getGamesStarted() {
        return gamesStarted;
    }
    
    public void setGamesStarted(Integer gamesStarted) {
        this.gamesStarted = gamesStarted;
    }
    
    public Double getMinutesPerGame() {
        return minutesPerGame;
    }
    
    public void setMinutesPerGame(Double minutesPerGame) {
        this.minutesPerGame = minutesPerGame;
    }
    
    public Double getFieldGoalsMade() {
        return fieldGoalsMade;
    }
    
    public void setFieldGoalsMade(Double fieldGoalsMade) {
        this.fieldGoalsMade = fieldGoalsMade;
    }
    
    public Double getFieldGoalsAttempted() {
        return fieldGoalsAttempted;
    }
    
    public void setFieldGoalsAttempted(Double fieldGoalsAttempted) {
        this.fieldGoalsAttempted = fieldGoalsAttempted;
    }
    
    public Double getFieldGoalPercentage() {
        return fieldGoalPercentage;
    }
    
    public void setFieldGoalPercentage(Double fieldGoalPercentage) {
        this.fieldGoalPercentage = fieldGoalPercentage;
    }
    
    public Double getThreePointersMade() {
        return threePointersMade;
    }
    
    public void setThreePointersMade(Double threePointersMade) {
        this.threePointersMade = threePointersMade;
    }
    
    public Double getThreePointersAttempted() {
        return threePointersAttempted;
    }
    
    public void setThreePointersAttempted(Double threePointersAttempted) {
        this.threePointersAttempted = threePointersAttempted;
    }
    
    public Double getThreePointPercentage() {
        return threePointPercentage;
    }
    
    public void setThreePointPercentage(Double threePointPercentage) {
        this.threePointPercentage = threePointPercentage;
    }
    
    public Double getTwoPointersMade() {
        return twoPointersMade;
    }
    
    public void setTwoPointersMade(Double twoPointersMade) {
        this.twoPointersMade = twoPointersMade;
    }
    
    public Double getTwoPointersAttempted() {
        return twoPointersAttempted;
    }
    
    public void setTwoPointersAttempted(Double twoPointersAttempted) {
        this.twoPointersAttempted = twoPointersAttempted;
    }
    
    public Double getTwoPointPercentage() {
        return twoPointPercentage;
    }
    
    public void setTwoPointPercentage(Double twoPointPercentage) {
        this.twoPointPercentage = twoPointPercentage;
    }
    
    public Double getEffectiveFieldGoalPercentage() {
        return effectiveFieldGoalPercentage;
    }
    
    public void setEffectiveFieldGoalPercentage(Double effectiveFieldGoalPercentage) {
        this.effectiveFieldGoalPercentage = effectiveFieldGoalPercentage;
    }
    
    public Double getFreeThrowsMade() {
        return freeThrowsMade;
    }
    
    public void setFreeThrowsMade(Double freeThrowsMade) {
        this.freeThrowsMade = freeThrowsMade;
    }
    
    public Double getFreeThrowsAttempted() {
        return freeThrowsAttempted;
    }
    
    public void setFreeThrowsAttempted(Double freeThrowsAttempted) {
        this.freeThrowsAttempted = freeThrowsAttempted;
    }
    
    public Double getFreeThrowPercentage() {
        return freeThrowPercentage;
    }
    
    public void setFreeThrowPercentage(Double freeThrowPercentage) {
        this.freeThrowPercentage = freeThrowPercentage;
    }
    
    public Double getOffensiveRebounds() {
        return offensiveRebounds;
    }
    
    public void setOffensiveRebounds(Double offensiveRebounds) {
        this.offensiveRebounds = offensiveRebounds;
    }
    
    public Double getDefensiveRebounds() {
        return defensiveRebounds;
    }
    
    public void setDefensiveRebounds(Double defensiveRebounds) {
        this.defensiveRebounds = defensiveRebounds;
    }
    
    public Double getTotalRebounds() {
        return totalRebounds;
    }
    
    public void setTotalRebounds(Double totalRebounds) {
        this.totalRebounds = totalRebounds;
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
    
    public Double getTurnovers() {
        return turnovers;
    }
    
    public void setTurnovers(Double turnovers) {
        this.turnovers = turnovers;
    }
    
    public Double getPersonalFouls() {
        return personalFouls;
    }
    
    public void setPersonalFouls(Double personalFouls) {
        this.personalFouls = personalFouls;
    }
    
    public Double getPoints() {
        return points;
    }
    
    public void setPoints(Double points) {
        this.points = points;
    }
    
    public String getAwards() {
        return awards;
    }
    
    public void setAwards(String awards) {
        this.awards = awards;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
