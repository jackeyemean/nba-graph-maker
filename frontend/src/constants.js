export const TEMPLATES = {
  player_comparison: {
    id: 'player_comparison',
    name: 'Player Comparison', 
    description: 'Compare multiple players\' statistics over their careers',
    graphType: 'line',
    fields: [
      { name: 'players', label: 'Players', type: 'multiselect', defaultValue: 'LeBron James,Stephen Curry' },
      { name: 'xAxisType', label: 'X-Axis', type: 'select', defaultValue: 'age' },
      { name: 'yAxisType', label: 'Y-Axis', type: 'select', defaultValue: 'points' }
    ]
  },
  season_distribution: {
    id: 'season_distribution',
    name: 'Histogram',
    description: 'Analyze the distribution of statistics across seasons',
    graphType: 'histogram',
    fields: [
      { name: 'years', label: 'Seasons', type: 'range', defaultValue: '1985,2025' },
      { name: 'stat', label: 'X-Axis', type: 'select', defaultValue: 'points' },
      { name: 'binCount', label: 'Number of Bins', type: 'number', defaultValue: '20' },
      { name: 'awards', label: 'Awards', type: 'multiselect', defaultValue: 'All' },
      { name: 'positions', label: 'Positions', type: 'multiselect', defaultValue: 'All' },
      { name: 'teamsFilter', label: 'Teams', type: 'multiselect', defaultValue: 'All' },
      { name: 'ageRange', label: 'Age', type: 'multiselect', defaultValue: 'All' },
      { name: 'minGamesPlayed', label: 'Min Games', type: 'number', defaultValue: '0' },
      { name: 'minMinutesPerGame', label: 'Min Minutes', type: 'number', defaultValue: '0' }
    ]
  },
  season_correlation: {
    id: 'season_correlation', 
    name: 'Scatter Plot',
    description: 'Find correlations and outliers between different statistics',
    graphType: 'scatter',
    fields: [
      { name: 'years', label: 'Seasons', type: 'range', defaultValue: '1985,2025' },
      { name: 'xAxisStat', label: 'X-Axis', type: 'select', defaultValue: 'turnovers' },
      { name: 'yAxisStat', label: 'Y-Axis', type: 'select', defaultValue: 'assists' },
      { name: 'awards', label: 'Awards', type: 'multiselect', defaultValue: 'All' },
      { name: 'positions', label: 'Positions', type: 'multiselect', defaultValue: 'All' },
      { name: 'teamsFilter', label: 'Teams', type: 'multiselect', defaultValue: 'All' },
      { name: 'ageRange', label: 'Age', type: 'multiselect', defaultValue: 'All' },
      { name: 'minGamesPlayed', label: 'Min Games', type: 'number', defaultValue: '0' },
      { name: 'minMinutesPerGame', label: 'Min Minutes', type: 'number', defaultValue: '0' }
    ]
  }
};

export const AVAILABLE_STATS = [
  'points', 'assists', 'rebounds', 'steals', 'blocks', 'turnovers',
  'field_goal_percentage', 'three_point_percentage', 'free_throw_percentage',
  'minutes_per_game', 'games_played', 'age'
];

export const POSITIONS = [
  'All', 'PG', 'SG', 'SF', 'PF', 'C', 'G', 'F', 'G-F', 'F-G'
];

export const AWARDS = [
  'All',
  'MVP-1', 'MVP-2', 'MVP-3', 'MVP-4', 'MVP-5',
  'AS',
  'DPOY-1', 'DPOY-2', 'DPOY-3', 'DPOY-4', 'DPOY-5', 
  'NBA-1', 'NBA-2', 'NBA-3',
  'DEF-1', 'DEF-2',
  'ROY', '6MOY-1', '6MOY-2', 'MIP'
];

export const generateYears = () => {
  const years = [];
  for (let year = 2025; year >= 1985; year--) {
    years.push(year.toString());
  }
  return years;
};

export const generateAgeRanges = () => {
  const ages = ['All'];
  for (let i = 18; i <= 44; i++) {
    ages.push(i.toString());
  }
  return ages;
};
