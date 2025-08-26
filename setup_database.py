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

def drop_existing_tables():
    """Drop all existing tables to start fresh"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Drop all tables if they exist
        cursor.execute("""
            DROP TABLE IF EXISTS nba_stats CASCADE;
        """)
        
        conn.commit()
        cursor.close()
        conn.close()
        
        print("Dropped existing tables")
        return True
        
    except Exception as e:
        print(f"Error dropping tables: {e}")
        return False

def create_unified_table():
    """Create one unified table for all NBA statistics"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        cursor.execute("""
            CREATE TABLE nba_stats (
                id SERIAL PRIMARY KEY,
                year INTEGER NOT NULL,
                player VARCHAR(255) NOT NULL,
                age INTEGER,
                team VARCHAR(10),
                position VARCHAR(10),
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
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("CREATE INDEX idx_nba_stats_year ON nba_stats(year)")
        cursor.execute("CREATE INDEX idx_nba_stats_player ON nba_stats(player)")
        cursor.execute("CREATE INDEX idx_nba_stats_team ON nba_stats(team)")
        cursor.execute("CREATE INDEX idx_nba_stats_position ON nba_stats(position)")
        cursor.execute("CREATE INDEX idx_nba_stats_year_player ON nba_stats(year, player)")
        cursor.execute("CREATE INDEX idx_nba_stats_games_minutes ON nba_stats(games_played, minutes_per_game)")
        cursor.execute("CREATE INDEX idx_nba_stats_age ON nba_stats(age)")
        cursor.execute("CREATE INDEX idx_nba_stats_awards ON nba_stats USING gin(string_to_array(awards, ','))")
        
        cursor.execute("CREATE INDEX idx_nba_stats_year_games_minutes ON nba_stats(year, games_played, minutes_per_game)")
        cursor.execute("CREATE INDEX idx_nba_stats_year_position ON nba_stats(year, position)")
        cursor.execute("CREATE INDEX idx_nba_stats_year_team ON nba_stats(year, team)")
        cursor.execute("CREATE INDEX idx_nba_stats_year_age ON nba_stats(year, age)")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        print("Unified NBA stats table created successfully")
        return True
        
    except Exception as e:
        print(f"Error creating unified table: {e}")
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
    """Import all CSV data into the unified table"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get all CSV files
        csv_files = glob.glob('data/NBA_*.csv')
        csv_files.sort()
        
        print(f"Found {len(csv_files)} CSV files to import")
        
        total_records = 0
        
        for csv_file in csv_files:
            year = extract_year_from_filename(csv_file)
            if not year:
                print(f"Could not extract year from {csv_file}, skipping...")
                continue
                
            print(f"Processing {csv_file} (Year: {year})")
            
            df = pd.read_csv(csv_file)
            
            # Process each row
            for _, row in df.iterrows():
                # Prepare data for insertion
                stats_data = {
                    'year': year,
                    'player': row['Player'],
                    'age': clean_value(row.get('Age')),
                    'team': clean_value(row.get('Team')),
                    'position': clean_value(row.get('Pos')),
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
                    INSERT INTO nba_stats (
                        year, player, age, team, position, games_played, games_started,
                        minutes_per_game, field_goals_made, field_goals_attempted, field_goal_percentage,
                        three_pointers_made, three_pointers_attempted, three_point_percentage,
                        two_pointers_made, two_pointers_attempted, two_point_percentage,
                        effective_field_goal_percentage, free_throws_made, free_throws_attempted,
                        free_throw_percentage, offensive_rebounds, defensive_rebounds, total_rebounds,
                        assists, steals, blocks, turnovers, personal_fouls, points, awards
                    ) VALUES (
                        %(year)s, %(player)s, %(age)s, %(team)s, %(position)s, %(games_played)s,
                        %(games_started)s, %(minutes_per_game)s, %(field_goals_made)s, %(field_goals_attempted)s,
                        %(field_goal_percentage)s, %(three_pointers_made)s, %(three_pointers_attempted)s,
                        %(three_point_percentage)s, %(two_pointers_made)s, %(two_pointers_attempted)s,
                        %(two_point_percentage)s, %(effective_field_goal_percentage)s, %(free_throws_made)s,
                        %(free_throws_attempted)s, %(free_throw_percentage)s, %(offensive_rebounds)s,
                        %(defensive_rebounds)s, %(total_rebounds)s, %(assists)s, %(steals)s, %(blocks)s,
                        %(turnovers)s, %(personal_fouls)s, %(points)s, %(awards)s
                    )
                """, stats_data)
                
                total_records += 1
            
            conn.commit()
            print(f"Completed processing {csv_file} - {len(df)} records")
        
        cursor.close()
        conn.close()
        
        print(f"Data import completed successfully! Total records: {total_records}")
        return True
        
    except Exception as e:
        print(f"Error importing data: {e}")
        return False

def show_database_stats():
    """Show statistics about the imported data"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get total count
        cursor.execute("SELECT COUNT(*) FROM nba_stats")
        total_count = cursor.fetchone()[0]
        
        # Get year range
        cursor.execute("SELECT MIN(year), MAX(year) FROM nba_stats")
        year_range = cursor.fetchone()
        
        # Get unique players count
        cursor.execute("SELECT COUNT(DISTINCT player) FROM nba_stats")
        unique_players = cursor.fetchone()[0]
        
        # Get unique teams count
        cursor.execute("SELECT COUNT(DISTINCT team) FROM nba_stats WHERE team IS NOT NULL")
        unique_teams = cursor.fetchone()[0]
        
        # Get years count
        cursor.execute("SELECT COUNT(DISTINCT year) FROM nba_stats")
        years_count = cursor.fetchone()[0]
        
        print("\n=== Database Statistics ===")
        print(f"Total Records: {total_count:,}")
        print(f"Unique Players: {unique_players:,}")
        print(f"Unique Teams: {unique_teams}")
        print(f"Years: {years_count} ({year_range[0]} - {year_range[1]})")
        
        # Show sample of recent data
        print("\n=== Sample Recent Data ===")
        cursor.execute("""
            SELECT year, player, team, position, points, games_played 
            FROM nba_stats 
            WHERE year >= 2020 
            ORDER BY year DESC, points DESC 
            LIMIT 10
        """)
        
        sample_data = cursor.fetchall()
        for row in sample_data:
            print(f"{row[0]}: {row[1]} ({row[2]}, {row[3]}) - {row[4]} PPG in {row[5]} games")
        
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"Error getting database stats: {e}")

def main():
    """Main function to set up the database and import data"""
    print("Starting NBA Database Reset and Setup...")
    
    # Step 1: Create database
    if not create_database():
        print("Failed to create database. Exiting.")
        return
    
    # Step 2: Drop existing tables
    if not drop_existing_tables():
        print("Failed to drop existing tables. Exiting.")
        return
    
    # Step 3: Create unified table
    if not create_unified_table():
        print("Failed to create unified table. Exiting.")
        return
    
    # Step 4: Import data
    if not import_data():
        print("Failed to import data. Exiting.")
        return
    
    # Step 5: Show statistics
    show_database_stats()
    
    print("\nDatabase reset and setup completed successfully!")
    print("\nYou can now connect to the database using:")
    print(f"Host: {DB_CONFIG['host']}")
    print(f"Database: {DB_CONFIG['database']}")
    print(f"User: {DB_CONFIG['user']}")
    print(f"Port: {DB_CONFIG['port']}")
    print("\nThe unified table 'nba_stats' contains all NBA data with a 'year' column.")

if __name__ == "__main__":
    main()


