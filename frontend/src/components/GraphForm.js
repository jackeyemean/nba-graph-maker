import React, { useState, useEffect } from 'react';
import './GraphForm.css';

const GraphForm = ({ template, onGenerateGraph, loading }) => {
  const [formData, setFormData] = useState({});
  const [options, setOptions] = useState({});
  const [playerSearchTerm, setPlayerSearchTerm] = useState('');
  const [filteredPlayers, setFilteredPlayers] = useState([]);
  const [showPlayerDropdown, setShowPlayerDropdown] = useState(false);
  const [yearSearchTerm, setYearSearchTerm] = useState('');
  const [filteredYears, setFilteredYears] = useState([]);
  const [showYearDropdown, setShowYearDropdown] = useState(false);
  const [positionSearchTerm, setPositionSearchTerm] = useState('');
  const [filteredPositions, setFilteredPositions] = useState([]);
  const [showPositionDropdown, setShowPositionDropdown] = useState(false);

  useEffect(() => {
    if (template) {
      initializeFormData();
      fetchOptions();
    }
  }, [template]);



  const initializeFormData = () => {
    const initialData = {
      graphType: template.graphType,
      template: template.id,
      title: `${template.name} Graph`,
      xAxisLabel: 'X-Axis',
      yAxisLabel: 'Y-Axis'
    };

    // Set default values from template
    if (template.defaultValues) {
      Object.assign(initialData, template.defaultValues);
    }

    // Set default values from fields
    if (template.fields) {
      template.fields.forEach(field => {
        if (field.defaultValue && !initialData[field.name]) {
          initialData[field.name] = field.defaultValue;
        }
      });
    }

    console.log('Initial form data:', initialData);
    console.log('Template fields:', template.fields);
    console.log('Template defaultValues:', template.defaultValues);
    console.log('xAxisStat in initial data:', initialData.xAxisStat);
    console.log('yAxisStat in initial data:', initialData.yAxisStat);
    setFormData(initialData);
  };

  const fetchOptions = async () => {
    try {
      const [statsResponse, playersResponse, teamsResponse, yearsResponse, positionsResponse] = await Promise.all([
        fetch('http://localhost:8080/api/graph/stats/options'),
        fetch('http://localhost:8080/api/graph/players'),
        fetch('http://localhost:8080/api/graph/teams'),
        fetch('http://localhost:8080/api/graph/years'),
        fetch('http://localhost:8080/api/graph/positions')
      ]);

      const [stats, players, teams, years, positions] = await Promise.all([
        statsResponse.json(),
        playersResponse.json(),
        teamsResponse.json(),
        yearsResponse.json(),
        positionsResponse.json()
      ]);

      setOptions({
        stats,
        players,
        teams,
        years: years.sort((a, b) => b - a), // Sort years descending
        positions
      });
    } catch (error) {
      console.error('Failed to fetch options:', error);
    }
  };

  const handleInputChange = (fieldName, value) => {
    console.log('handleInputChange called:', fieldName, value);
    setFormData(prev => {
      const newData = {
        ...prev,
        [fieldName]: value
      };
      console.log('Updated formData:', newData);
      
      // Debug: Check if xAxisStat or yAxisStat are being updated
      if (fieldName === 'xAxisStat') {
        console.log('xAxisStat updated to:', value);
      }
      if (fieldName === 'yAxisStat') {
        console.log('yAxisStat updated to:', value);
      }
      
      return newData;
    });
  };

  const handlePlayerSearch = (searchTerm, fieldName) => {
    // Always update the search term state first
    if (fieldName === 'years') {
      setYearSearchTerm(searchTerm);
    } else if (fieldName === 'positions') {
      setPositionSearchTerm(searchTerm);
    } else {
      setPlayerSearchTerm(searchTerm);
    }

    if (searchTerm.length < 1) {
      if (fieldName === 'years') {
        setFilteredYears([]);
        setShowYearDropdown(false);
      } else if (fieldName === 'positions') {
        setFilteredPositions([]);
        setShowPositionDropdown(false);
      } else {
        setFilteredPlayers([]);
        setShowPlayerDropdown(false);
      }
      return;
    }

    // Determine which options to search based on current field
    let searchOptions = [];
    if (fieldName === 'years') {
      searchOptions = options.years || [];
    } else if (fieldName === 'positions') {
      searchOptions = options.positions || [];
    } else {
      searchOptions = options.players || [];
    }
    
    const filtered = searchOptions.filter(option =>
      option.toString().toLowerCase().includes(searchTerm.toLowerCase())
    ).slice(0, 10); // Limit to 10 results

    if (fieldName === 'years') {
      setFilteredYears(filtered);
      setShowYearDropdown(filtered.length > 0);
    } else if (fieldName === 'positions') {
      setFilteredPositions(filtered);
      setShowPositionDropdown(filtered.length > 0);
    } else {
      setFilteredPlayers(filtered);
      setShowPlayerDropdown(filtered.length > 0);
    }
  };

  const addPlayer = (playerName, fieldName) => {
    if (fieldName === 'years') {
      // Handle years selection
      const currentYears = formData.years || '';
      const yearList = currentYears ? currentYears.split(',').map(y => y.trim()).filter(y => y) : [];
      
      if (!yearList.includes(playerName.toString())) {
        const newYearList = [...yearList, playerName.toString()];
        handleInputChange('years', newYearList.join(', '));
      }
    } else if (fieldName === 'positions') {
      // Handle positions selection
      const currentPositions = formData.positions || '';
      const positionList = currentPositions ? currentPositions.split(',').map(p => p.trim()).filter(p => p) : [];
      
      if (!positionList.includes(playerName)) {
        const newPositionList = [...positionList, playerName];
        handleInputChange('positions', newPositionList.join(', '));
      }
    } else {
      // Handle players selection
      const currentPlayers = formData.players || '';
      const playerList = currentPlayers ? currentPlayers.split(',').map(p => p.trim()).filter(p => p) : [];
      
      if (!playerList.includes(playerName)) {
        const newPlayerList = [...playerList, playerName];
        handleInputChange('players', newPlayerList.join(', '));
      }
    }
    
    // Clear search term and close dropdown
    if (fieldName === 'years') {
      setYearSearchTerm('');
      setShowYearDropdown(false);
    } else if (fieldName === 'positions') {
      setPositionSearchTerm('');
      setShowPositionDropdown(false);
    } else {
      setPlayerSearchTerm('');
      setShowPlayerDropdown(false);
    }
  };

  const removePlayer = (playerToRemove, fieldName) => {
    if (fieldName === 'years') {
      const currentYears = formData.years || '';
      const yearList = currentYears.split(',').map(y => y.trim()).filter(y => y !== playerToRemove);
      handleInputChange('years', yearList.join(', '));
    } else if (fieldName === 'positions') {
      const currentPositions = formData.positions || '';
      const positionList = currentPositions.split(',').map(p => p.trim()).filter(p => p !== playerToRemove);
      handleInputChange('positions', positionList.join(', '));
    } else {
      const currentPlayers = formData.players || '';
      const playerList = currentPlayers.split(',').map(p => p.trim()).filter(p => p !== playerToRemove);
      handleInputChange('players', playerList.join(', '));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!loading) {
      // Convert players string to array if it exists
      const processedData = { ...formData };
      if (processedData.players && typeof processedData.players === 'string') {
        processedData.players = processedData.players.split(',').map(p => p.trim()).filter(p => p);
      }
      if (processedData.years && typeof processedData.years === 'string') {
        processedData.years = processedData.years.split(',').map(y => parseInt(y.trim())).filter(y => !isNaN(y));
      }
      if (processedData.positions && typeof processedData.positions === 'string') {
        processedData.positions = processedData.positions.split(',').map(p => p.trim()).filter(p => p);
      }
      
      // Ensure we have the current form values
      console.log('Current formData:', formData);
      console.log('Submitting form data:', processedData);
      console.log('Form data keys:', Object.keys(processedData));
      console.log('xAxisStat value:', processedData.xAxisStat);
      console.log('yAxisStat value:', processedData.yAxisStat);
      console.log('xAxisLabel value:', processedData.xAxisLabel);
      console.log('yAxisLabel value:', processedData.yAxisLabel);
      
      // Debug: Check if xAxisStat and yAxisStat are actually set
      if (!processedData.xAxisStat) {
        console.warn('WARNING: xAxisStat is not set!');
      }
      if (!processedData.yAxisStat) {
        console.warn('WARNING: yAxisStat is not set!');
      }
      
      onGenerateGraph(processedData);
    }
  };

  const renderField = (field) => {
    const value = formData[field.name] || '';

    switch (field.type) {
      case 'text':
        return (
          <input
            type="text"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.description}
            required={field.required}
          />
        );

      case 'number':
        return (
          <input
            type="number"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.description}
            required={field.required}
          />
        );

      case 'select':
        let selectOptions = [];
        
        if (field.name === 'year' || field.name === 'years') {
          selectOptions = options.years || [];
        } else if (field.name === 'stat' || field.name === 'xAxisStat' || field.name === 'yAxisStat') {
          selectOptions = options.stats || [];
        } else if (field.name === 'xAxisType') {
          selectOptions = ['age', 'year']; // Only age and year make sense for X-axis in line graphs
        } else if (field.name === 'yAxisType') {
          selectOptions = ['age', 'year', 'points', 'assists', 'rebounds', 'steals', 'blocks', 'minutes_per_game', 'field_goal_percentage', 'three_point_percentage', 'free_throw_percentage', 'turnovers', 'personal_fouls'];
        }
        
        return (
          <select
            value={value}
            onChange={(e) => {
              console.log('Select onChange triggered:', field.name, e.target.value);
              handleInputChange(field.name, e.target.value);
            }}
            required={field.required}
          >
            <option value="">Select {field.label}</option>
            {selectOptions.map(option => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        );

                  case 'multiselect':
        if (field.name === 'players' || field.name === 'years' || field.name === 'positions') {
          return (
            <div className="multiselect-container">
              <div className="search-container">
                <input
                   type="text"
                   placeholder={
                     field.name === 'years' ? "Search years..." : 
                     field.name === 'positions' ? "Search positions..." : 
                     "Search players..."
                   }
                   value={
                     field.name === 'years' ? yearSearchTerm :
                     field.name === 'positions' ? positionSearchTerm :
                     playerSearchTerm
                   }
                   onChange={(e) => handlePlayerSearch(e.target.value, field.name)}
                   onFocus={() => {
                     const currentSearchTerm = 
                       field.name === 'years' ? yearSearchTerm :
                       field.name === 'positions' ? positionSearchTerm :
                       playerSearchTerm;
                     if (currentSearchTerm.length >= 1) {
                       if (field.name === 'years') {
                         setShowYearDropdown(true);
                       } else if (field.name === 'positions') {
                         setShowPositionDropdown(true);
                       } else {
                         setShowPlayerDropdown(true);
                       }
                     }
                   }}
                  onBlur={() => {
                    // Delay hiding dropdown to allow clicking on options
                    setTimeout(() => {
                      if (field.name === 'years') {
                        setShowYearDropdown(false);
                      } else if (field.name === 'positions') {
                        setShowPositionDropdown(false);
                      } else {
                        setShowPlayerDropdown(false);
                      }
                    }, 200);
                  }}
                />
                {(field.name === 'years' ? showYearDropdown : 
                  field.name === 'positions' ? showPositionDropdown : 
                  showPlayerDropdown) && (
                  <div className="player-dropdown">
                    {(field.name === 'years' ? filteredYears : 
                      field.name === 'positions' ? filteredPositions : 
                      filteredPlayers).map((item, index) => (
                       <div
                         key={index}
                         className="player-option"
                         onClick={() => addPlayer(item, field.name)}
                       >
                         {item}
                       </div>
                     ))}
                  </div>
                )}
              </div>
              <div className="selected-items">
                {value.split(',').filter(p => p.trim()).map((item, index) => (
                  <span key={index} className="selected-item">
                    {item.trim()}
                    <button
                      type="button"
                      onClick={() => removePlayer(item.trim(), field.name)}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            </div>
          );
        }
        return null;

      case 'checkbox':
        return (
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={value === 'true' || value === true}
              onChange={(e) => handleInputChange(field.name, e.target.checked.toString())}
            />
            {field.label}
          </label>
        );

      default:
        return null;
    }
  };

  if (!template) return null;

  return (
    <div className="graph-form">
      <h2>{template.name}</h2>
      <p>{template.description}</p>
      
      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          {template.fields && template.fields.map((field) => (
            <div key={field.name} className="form-field">
              <label htmlFor={field.name}>
                {field.label}
                {field.required && <span className="required">*</span>}
              </label>
              {renderField(field)}
              {field.description && (
                <small className="field-description">{field.description}</small>
              )}
            </div>
          ))}
        </div>
        
        <div className="form-actions">
          <button 
            type="submit" 
            className="generate-button"
            disabled={loading}
          >
            {loading ? 'Generating...' : 'Generate Graph'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default GraphForm;

