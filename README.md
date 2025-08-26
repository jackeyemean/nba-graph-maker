## Database Structure

### Table: `nba_stats`

The main table containing all NBA player statistics with the following columns:

| Column | Type | Description |
|--------|------|-------------|
| `id` | SERIAL PRIMARY KEY | Unique identifier for each record |
| `year` | INTEGER NOT NULL | NBA season year (e.g., 2024 for 2023-24 season) |
| `player` | VARCHAR(255) NOT NULL | Player's full name |
| `age` | INTEGER | Player's age during the season |
| `team` | VARCHAR(10) | Team abbreviation (e.g., "LAL", "BOS") |
| `position` | VARCHAR(10) | Player's position (e.g., "PG", "SG", "SF", "PF", "C") |
| `games_played` | INTEGER | Number of games played |
| `games_started` | INTEGER | Number of games started |
| `minutes_per_game` | DECIMAL(4,1) | Average minutes per game |
| `field_goals_made` | DECIMAL(4,1) | Field goals made per game |
| `field_goals_attempted` | DECIMAL(4,1) | Field goals attempted per game |
| `field_goal_percentage` | DECIMAL(4,3) | Field goal percentage |
| `three_pointers_made` | DECIMAL(4,1) | Three-pointers made per game |
| `three_pointers_attempted` | DECIMAL(4,1) | Three-pointers attempted per game |
| `three_point_percentage` | DECIMAL(4,3) | Three-point percentage |
| `two_pointers_made` | DECIMAL(4,1) | Two-pointers made per game |
| `two_pointers_attempted` | DECIMAL(4,1) | Two-pointers attempted per game |
| `two_point_percentage` | DECIMAL(4,3) | Two-point percentage |
| `effective_field_goal_percentage` | DECIMAL(4,3) | Effective field goal percentage |
| `free_throws_made` | DECIMAL(4,1) | Free throws made per game |
| `free_throws_attempted` | DECIMAL(4,1) | Free throws attempted per game |
| `free_throw_percentage` | DECIMAL(4,3) | Free throw percentage |
| `offensive_rebounds` | DECIMAL(4,1) | Offensive rebounds per game |
| `defensive_rebounds` | DECIMAL(4,1) | Defensive rebounds per game |
| `total_rebounds` | DECIMAL(4,1) | Total rebounds per game |
| `assists` | DECIMAL(4,1) | Assists per game |
| `steals` | DECIMAL(4,1) | Steals per game |
| `blocks` | DECIMAL(4,1) | Blocks per game |
| `turnovers` | DECIMAL(4,1) | Turnovers per game |
| `personal_fouls` | DECIMAL(4,1) | Personal fouls per game |
| `points` | DECIMAL(4,1) | Points per game |
| `awards` | TEXT | Awards and accolades (e.g., "MVP-1,AS,NBA1") |
| `created_at` | TIMESTAMP | Record creation timestamp |

## Important Notes

### Multi-Team Players
For players who played for multiple teams in a single season, the database contains **multiple records**:

1. **Individual Team Records**: One record for each team the player played for
2. **Combined Season Record**: One additional record with combined statistics for the entire season

**Example**: If a player played for 2 teams in a season, they will have 3 records:
- Record 1: Stats with Team A
- Record 2: Stats with Team B  
- Record 3: Combined season stats with team = "2TM"

## Indexes
The following indexes are created for optimal query performance:
- `idx_nba_stats_year` - For filtering by year
- `idx_nba_stats_player` - For filtering by player
- `idx_nba_stats_team` - For filtering by team
- `idx_nba_stats_position` - For filtering by position
- `idx_nba_stats_year_player` - For queries combining year and player

## Notes
- All percentage values are stored as decimals (e.g., 0.450 for 45.0%)
- NULL values indicate missing or unavailable data
