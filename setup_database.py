import os
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import glob
from datetime import datetime
import re

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'database': 'nba_stats',
    'user': 'postgres',
    'password': '4236',
    'port': '5432'
}

def create_database():
    """Create the database if it doesn't exist"""
    try:
        # Connect to default postgres database
        conn = psycopg2.connect(
            host=DB_CONFIG['host'],
            database='postgres',
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password'],
            port=DB_CONFIG['port']
        )
        conn.autocommit = True
        cursor = conn.cursor()
        
        # Check if database exists
        cursor.execute("SELECT 1 FROM pg_database WHERE datname = %s", (DB_CONFIG['database'],))
        exists = cursor.fetchone()
        
        if not exists:
            cursor.execute(f"CREATE DATABASE {DB_CONFIG['database']}")
            print(f"Database '{DB_CONFIG['database']}' created successfully")
        else:
            print(f"Database '{DB_CONFIG['database']}' already exists")
            
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"Error creating database: {e}")
        return False
    
    return True

def create_tables():
    """Create the necessary tables"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Create players table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS players (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        # Create teams table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS teams (
                id SERIAL PRIMARY KEY,
                abbreviation VARCHAR(10) UNIQUE NOT NULL,
                name VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        # Create seasons table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS seasons (
                id SERIAL PRIMARY KEY,
                year INTEGER UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        # Create player_stats table (main table for all statistics)
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS player_stats (
                id SERIAL PRIMARY KEY,
                player_id INTEGER REFERENCES players(id),
                team_id INTEGER REFERENCES teams(id),
                season_id INTEGER REFERENCES seasons(id),
                position VARCHAR(10),
                age INTEGER,
                games_played INTEGER,
                games_started INTEGER,
                minutes_per_game DECIMAL(4,1),
                field_goals_made DECIMAL(4,1),
                field_goals_attempted DECIMAL(4,1),
                field_goal_percentage DECIMAL(4,3),
                three_pointers_made DECIMAL(4,1),
                three_pointers_attempted DECIMAL(4,1),
                three_point_percentage DECIMAL(4,3),
                two_pointers_made DECIMAL(4,1),
                two_pointers_attempted DECIMAL(4,1),
                two_point_percentage DECIMAL(4,3),
                effective_field_goal_percentage DECIMAL(4,3),
                free_throws_made DECIMAL(4,1),
                free_throws_attempted DECIMAL(4,1),
                free_throw_percentage DECIMAL(4,3),
                offensive_rebounds DECIMAL(4,1),
                defensive_rebounds DECIMAL(4,1),
                total_rebounds DECIMAL(4,1),
                assists DECIMAL(4,1),
                steals DECIMAL(4,1),
                blocks DECIMAL(4,1),
                turnovers DECIMAL(4,1),
                personal_fouls DECIMAL(4,1),
                points DECIMAL(4,1),
                awards TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(player_id, team_id, season_id)
            )
        """)
        
        # Create indexes for better performance
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_player_id ON player_stats(player_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_season_id ON player_stats(season_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_team_id ON player_stats(team_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_position ON player_stats(position)")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        print("Tables created successfully")
        return True
        
    except Exception as e:
        print(f"Error creating tables: {e}")
        return False

def extract_year_from_filename(filename):
    """Extract year from filename like 'NBA_2024_per_game_stats.csv'"""
    match = re.search(r'NBA_(\d{4})_per_game_stats\.csv', filename)
    return int(match.group(1)) if match else None

def clean_value(value):
    """Clean and convert values appropriately"""
    if pd.isna(value) or value == '':
        return None
    
    # Convert string to appropriate type
    if isinstance(value, str):
        value = value.strip()
        if value == '':
            return None
        
        # Handle percentage values (remove % and convert to decimal)
        if value.startswith('.'):
            try:
                return float(value)
            except:
                return None
        
        # Handle numeric values
        try:
            if '.' in value:
                return float(value)
            else:
                return int(value)
        except:
            return value
    
    return value

def import_data():
    """Import all CSV data into the database"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get all CSV files
        csv_files = glob.glob('data/NBA_*.csv')
        csv_files.sort()  # Sort to process in chronological order
        
        print(f"Found {len(csv_files)} CSV files to import")
        
        for csv_file in csv_files:
            year = extract_year_from_filename(csv_file)
            if not year:
                print(f"Could not extract year from {csv_file}, skipping...")
                continue
                
            print(f"Processing {csv_file} (Year: {year})")
            
            # Read CSV file
            df = pd.read_csv(csv_file)
            
            # Insert season if not exists
            cursor.execute("INSERT INTO seasons (year) VALUES (%s) ON CONFLICT (year) DO NOTHING", (year,))
            cursor.execute("SELECT id FROM seasons WHERE year = %s", (year,))
            season_id = cursor.fetchone()[0]
            
            # Process each row
            for _, row in df.iterrows():
                player_name = row['Player']
                
                # Insert player if not exists
                cursor.execute("INSERT INTO players (name) VALUES (%s) ON CONFLICT DO NOTHING", (player_name,))
                cursor.execute("SELECT id FROM players WHERE name = %s", (player_name,))
                player_id = cursor.fetchone()[0]
                
                # Insert team if not exists
                team_abbr = row.get('Team', '')
                if team_abbr and team_abbr != '':
                    cursor.execute("INSERT INTO teams (abbreviation) VALUES (%s) ON CONFLICT (abbreviation) DO NOTHING", (team_abbr,))
                    cursor.execute("SELECT id FROM teams WHERE abbreviation = %s", (team_abbr,))
                    team_id = cursor.fetchone()[0]
                else:
                    team_id = None
                
                # Prepare data for insertion
                stats_data = {
                    'player_id': player_id,
                    'team_id': team_id,
                    'season_id': season_id,
                    'position': clean_value(row.get('Pos')),
                    'age': clean_value(row.get('Age')),
                    'games_played': clean_value(row.get('G')),
                    'games_started': clean_value(row.get('GS')),
                    'minutes_per_game': clean_value(row.get('MP')),
                    'field_goals_made': clean_value(row.get('FG')),
                    'field_goals_attempted': clean_value(row.get('FGA')),
                    'field_goal_percentage': clean_value(row.get('FG%')),
                    'three_pointers_made': clean_value(row.get('3P')),
                    'three_pointers_attempted': clean_value(row.get('3PA')),
                    'three_point_percentage': clean_value(row.get('3P%')),
                    'two_pointers_made': clean_value(row.get('2P')),
                    'two_pointers_attempted': clean_value(row.get('2PA')),
                    'two_point_percentage': clean_value(row.get('2P%')),
                    'effective_field_goal_percentage': clean_value(row.get('eFG%')),
                    'free_throws_made': clean_value(row.get('FT')),
                    'free_throws_attempted': clean_value(row.get('FTA')),
                    'free_throw_percentage': clean_value(row.get('FT%')),
                    'offensive_rebounds': clean_value(row.get('ORB')),
                    'defensive_rebounds': clean_value(row.get('DRB')),
                    'total_rebounds': clean_value(row.get('TRB')),
                    'assists': clean_value(row.get('AST')),
                    'steals': clean_value(row.get('STL')),
                    'blocks': clean_value(row.get('BLK')),
                    'turnovers': clean_value(row.get('TOV')),
                    'personal_fouls': clean_value(row.get('PF')),
                    'points': clean_value(row.get('PTS')),
                    'awards': clean_value(row.get('Awards'))
                }
                
                # Insert player stats
                cursor.execute("""
                    INSERT INTO player_stats (
                        player_id, team_id, season_id, position, age, games_played, games_started,
                        minutes_per_game, field_goals_made, field_goals_attempted, field_goal_percentage,
                        three_pointers_made, three_pointers_attempted, three_point_percentage,
                        two_pointers_made, two_pointers_attempted, two_point_percentage,
                        effective_field_goal_percentage, free_throws_made, free_throws_attempted,
                        free_throw_percentage, offensive_rebounds, defensive_rebounds, total_rebounds,
                        assists, steals, blocks, turnovers, personal_fouls, points, awards
                    ) VALUES (
                        %(player_id)s, %(team_id)s, %(season_id)s, %(position)s, %(age)s, %(games_played)s,
                        %(games_started)s, %(minutes_per_game)s, %(field_goals_made)s, %(field_goals_attempted)s,
                        %(field_goal_percentage)s, %(three_pointers_made)s, %(three_pointers_attempted)s,
                        %(three_point_percentage)s, %(two_pointers_made)s, %(two_pointers_attempted)s,
                        %(two_point_percentage)s, %(effective_field_goal_percentage)s, %(free_throws_made)s,
                        %(free_throws_attempted)s, %(free_throw_percentage)s, %(offensive_rebounds)s,
                        %(defensive_rebounds)s, %(total_rebounds)s, %(assists)s, %(steals)s, %(blocks)s,
                        %(turnovers)s, %(personal_fouls)s, %(points)s, %(awards)s
                    ) ON CONFLICT (player_id, team_id, season_id) DO UPDATE SET
                        position = EXCLUDED.position,
                        age = EXCLUDED.age,
                        games_played = EXCLUDED.games_played,
                        games_started = EXCLUDED.games_started,
                        minutes_per_game = EXCLUDED.minutes_per_game,
                        field_goals_made = EXCLUDED.field_goals_made,
                        field_goals_attempted = EXCLUDED.field_goals_attempted,
                        field_goal_percentage = EXCLUDED.field_goal_percentage,
                        three_pointers_made = EXCLUDED.three_pointers_made,
                        three_pointers_attempted = EXCLUDED.three_pointers_attempted,
                        three_point_percentage = EXCLUDED.three_point_percentage,
                        two_pointers_made = EXCLUDED.two_pointers_made,
                        two_pointers_attempted = EXCLUDED.two_pointers_attempted,
                        two_point_percentage = EXCLUDED.two_point_percentage,
                        effective_field_goal_percentage = EXCLUDED.effective_field_goal_percentage,
                        free_throws_made = EXCLUDED.free_throws_made,
                        free_throws_attempted = EXCLUDED.free_throws_attempted,
                        free_throw_percentage = EXCLUDED.free_throw_percentage,
                        offensive_rebounds = EXCLUDED.offensive_rebounds,
                        defensive_rebounds = EXCLUDED.defensive_rebounds,
                        total_rebounds = EXCLUDED.total_rebounds,
                        assists = EXCLUDED.assists,
                        steals = EXCLUDED.steals,
                        blocks = EXCLUDED.blocks,
                        turnovers = EXCLUDED.turnovers,
                        personal_fouls = EXCLUDED.personal_fouls,
                        points = EXCLUDED.points,
                        awards = EXCLUDED.awards
                """, stats_data)
            
            conn.commit()
            print(f"Completed processing {csv_file}")
        
        cursor.close()
        conn.close()
        
        print("Data import completed successfully!")
        return True
        
    except Exception as e:
        print(f"Error importing data: {e}")
        return False

def show_database_stats():
    """Show statistics about the imported data"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get counts
        cursor.execute("SELECT COUNT(*) FROM players")
        player_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM teams")
        team_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM seasons")
        season_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM player_stats")
        stats_count = cursor.fetchone()[0]
        
        # Get year range
        cursor.execute("SELECT MIN(year), MAX(year) FROM seasons")
        year_range = cursor.fetchone()
        
        print("\n=== Database Statistics ===")
        print(f"Players: {player_count}")
        print(f"Teams: {team_count}")
        print(f"Seasons: {season_count} ({year_range[0]} - {year_range[1]})")
        print(f"Player Statistics Records: {stats_count}")
        
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"Error getting database stats: {e}")

def main():
    """Main function to set up the database and import data"""
    print("Starting NBA Database Setup...")
    
    # Step 1: Create database
    if not create_database():
        print("Failed to create database. Exiting.")
        return
    
    # Step 2: Create tables
    if not create_tables():
        print("Failed to create tables. Exiting.")
        return
    
    # Step 3: Import data
    if not import_data():
        print("Failed to import data. Exiting.")
        return
    
    # Step 4: Show statistics
    show_database_stats()
    
    print("\nDatabase setup completed successfully!")
    print("\nYou can now connect to the database using:")
    print(f"Host: {DB_CONFIG['host']}")
    print(f"Database: {DB_CONFIG['database']}")
    print(f"User: {DB_CONFIG['user']}")
    print(f"Port: {DB_CONFIG['port']}")

if __name__ == "__main__":
    main()


