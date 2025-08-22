package com.nba.repository;

import com.nba.entity.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    
    // Find all stats for a specific player
    
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.player = :playerName ORDER BY ps.year")
    List<PlayerStats> findByPlayerName(@Param("playerName") String playerName);
    
    // Find all stats for a specific year
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.year = :year ORDER BY ps.points DESC")
    List<PlayerStats> findByYear(@Param("year") Integer year);
    
    // Find all stats for a specific player and year
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.player = :playerName AND ps.year = :year")
    List<PlayerStats> findByPlayerAndYear(@Param("playerName") String playerName, @Param("year") Integer year);
    
    // Find all stats for a specific team and year
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.team = :team AND ps.year = :year ORDER BY ps.points DESC")
    List<PlayerStats> findByTeamAndYear(@Param("team") String team, @Param("year") Integer year);
    
    // Find all unique players
    @Query("SELECT DISTINCT ps.player FROM PlayerStats ps ORDER BY ps.player")
    List<String> findAllPlayers();
    
    // Find all unique teams
    @Query("SELECT DISTINCT ps.team FROM PlayerStats ps WHERE ps.team IS NOT NULL ORDER BY ps.team")
    List<String> findAllTeams();
    

    
    // Search players by name (case-insensitive)
    @Query("SELECT DISTINCT ps.player FROM PlayerStats ps WHERE LOWER(ps.player) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY ps.player")
    List<String> searchPlayers(@Param("search") String search);
    
    // Find stats for a specific year with minimum games played
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.year = :year AND ps.gamesPlayed >= :minGames ORDER BY ps.points DESC")
    List<PlayerStats> findByYearAndMinGames(@Param("year") Integer year, @Param("minGames") Integer minGames);
    
    // Find stats for a specific year, excluding multi-team players
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.year = :year AND (ps.team IS NULL OR ps.team NOT LIKE '%TM') ORDER BY ps.points DESC")
    List<PlayerStats> findByYearExcludingMultiTeam(@Param("year") Integer year);
    
    // Find stats for a specific year, excluding multi-team players, with minimum games
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.year = :year AND (ps.team IS NULL OR ps.team NOT LIKE '%TM') AND ps.gamesPlayed >= :minGames ORDER BY ps.points DESC")
    List<PlayerStats> findByYearExcludingMultiTeamWithMinGames(@Param("year") Integer year, @Param("minGames") Integer minGames);
    
    // Find player career stats (all years for a player)
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.player = :playerName AND (ps.team IS NULL OR ps.team NOT LIKE '%TM') ORDER BY ps.year")
    List<PlayerStats> findPlayerCareerStats(@Param("playerName") String playerName);
    
    // Find player career stats including multi-team seasons
    @Query("SELECT ps FROM PlayerStats ps WHERE ps.player = :playerName ORDER BY ps.year")
    List<PlayerStats> findPlayerAllStats(@Param("playerName") String playerName);
    

    
    // Find all unique awards
    @Query("SELECT DISTINCT unnest(string_to_array(ps.awards, ',')) FROM PlayerStats ps WHERE ps.awards IS NOT NULL AND ps.awards != '' ORDER BY 1")
    List<String> findAllAwards();
    
    // Find filtered awards (top 5 for MVP, DPOY, 6MOY, and exclude high place numbers)
    @Query("SELECT DISTINCT unnest(string_to_array(ps.awards, ',')) FROM PlayerStats ps WHERE ps.awards IS NOT NULL AND ps.awards != '' ORDER BY 1")
    List<String> findFilteredAwards();
}
