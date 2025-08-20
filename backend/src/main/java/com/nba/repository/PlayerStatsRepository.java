package com.nba.repository;

import com.nba.entity.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE p.name LIKE %:playerName% ORDER BY ps.season")
    List<PlayerStats> findByPlayerNameContainingOrderBySeason(@Param("playerName") String playerName);
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE p.name = :playerName ORDER BY ps.season")
    List<PlayerStats> findByPlayerNameOrderBySeason(@Param("playerName") String playerName);
    
    @Query("SELECT DISTINCT p.name FROM Player p WHERE p.name LIKE %:playerName%")
    List<String> findPlayerNamesContaining(@Param("playerName") String playerName);
    
    @Query("SELECT DISTINCT ps.season FROM PlayerStats ps ORDER BY ps.season")
    List<String> findAllSeasons();
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE p.team = :team ORDER BY ps.season")
    List<PlayerStats> findByTeamOrderBySeason(@Param("team") String team);
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE p.position = :position ORDER BY ps.season")
    List<PlayerStats> findByPositionOrderBySeason(@Param("position") String position);
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE ps.season = :season ORDER BY ps.points DESC")
    List<PlayerStats> findBySeasonOrderByPointsDesc(@Param("season") String season);
    
    @Query("SELECT ps FROM PlayerStats ps JOIN ps.player p WHERE ps.season = :season")
    List<PlayerStats> findBySeason(@Param("season") String season);
}
