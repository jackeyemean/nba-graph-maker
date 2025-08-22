import React, { useState, useEffect } from 'react';
import './GraphForm.css';
import { TEMPLATES, AVAILABLE_STATS, POSITIONS, AWARDS, generateYears, generateAgeRanges } from '../constants';

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
  const [awardSearchTerm, setAwardSearchTerm] = useState('');
  const [filteredAwards, setFilteredAwards] = useState([]);
  const [showAwardDropdown, setShowAwardDropdown] = useState(false);
  const [ageRangeSearchTerm, setAgeRangeSearchTerm] = useState('');
  const [filteredAgeRanges, setFilteredAgeRanges] = useState([]);
  const [showAgeRangeDropdown, setShowAgeRangeDropdown] = useState(false);
  const [teamsFilterSearchTerm, setTeamsFilterSearchTerm] = useState('');
  const [filteredTeamsFilter, setFilteredTeamsFilter] = useState([]);
  const [showTeamsFilterDropdown, setShowTeamsFilterDropdown] = useState(false);
  const [seasonRange, setSeasonRange] = useState([1950, 2025]);

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

    // Initialize season range for range slider
    if (template.fields && template.fields.some(field => field.name === 'years' && field.type === 'range')) {
      setSeasonRange([1950, 2025]);
      // Initialize years field with the full range
      const years = [];
      for (let year = 1950; year <= 2025; year++) {
        years.push(year);
      }
      initialData.years = years.join(', ');
    }

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
        // Set specific defaults
        if (field.name === 'positions' && !initialData[field.name]) {
          initialData[field.name] = 'All';
        }
        if (field.name === 'awards' && !initialData[field.name]) {
          initialData[field.name] = 'All';
        }
        if (field.name === 'ageRange' && !initialData[field.name]) {
          initialData[field.name] = 'All';
        }
        if (field.name === 'teamsFilter' && !initialData[field.name]) {
          initialData[field.name] = 'All';
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
      // Only fetch dynamic data from API
      const [playersResponse, teamsResponse] = await Promise.all([
        fetch('http://localhost:8080/api/graph/players'),
        fetch('http://localhost:8080/api/graph/teams')
      ]);

      const [players, teams] = await Promise.all([
        playersResponse.json(),
        teamsResponse.json()
      ]);

      setOptions({
        // Use hardcoded constants instead of API calls
        stats: AVAILABLE_STATS,
        players,
        teams,
        years: generateYears(),
        positions: POSITIONS,
        awards: AWARDS,
        ageRanges: generateAgeRanges(),
        teamsFilter: ['All', ...teams.sort()] // Sort teams alphabetically
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
    } else if (fieldName === 'awards') {
      setAwardSearchTerm(searchTerm);
    } else      if (fieldName === 'ageRange') {
       setAgeRangeSearchTerm(searchTerm);
     } else if (fieldName === 'teamsFilter') {
       setTeamsFilterSearchTerm(searchTerm);
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
      } else if (fieldName === 'awards') {
        setFilteredAwards([]);
        setShowAwardDropdown(false);
             } else if (fieldName === 'ageRange') {
         setFilteredAgeRanges([]);
         setShowAgeRangeDropdown(false);
       } else if (fieldName === 'teamsFilter') {
         setFilteredTeamsFilter([]);
         setShowTeamsFilterDropdown(false);
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
    } else if (fieldName === 'awards') {
      searchOptions = options.awards || [];
         } else if (fieldName === 'ageRange') {
       searchOptions = options.ageRanges || [];
     } else if (fieldName === 'teamsFilter') {
       searchOptions = options.teamsFilter || [];
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
    } else if (fieldName === 'awards') {
      setFilteredAwards(filtered);
      setShowAwardDropdown(filtered.length > 0);
         } else if (fieldName === 'ageRange') {
       setFilteredAgeRanges(filtered);
       setShowAgeRangeDropdown(filtered.length > 0);
     } else if (fieldName === 'teamsFilter') {
       setFilteredTeamsFilter(filtered);
       setShowTeamsFilterDropdown(filtered.length > 0);
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
       if (playerName === 'All') {
         handleInputChange('positions', 'All');
       } else {
         const currentPositions = formData.positions || '';
         const positionList = currentPositions ? currentPositions.split(',').map(p => p.trim()).filter(p => p && p !== 'All') : [];
         
         if (!positionList.includes(playerName)) {
           const newPositionList = [...positionList, playerName];
           handleInputChange('positions', newPositionList.join(', '));
         }
       }
       // Keep dropdown open for multi-select
       setShowPositionDropdown(true);
     } else if (fieldName === 'awards') {
       // Handle awards selection
       if (playerName === 'All') {
         handleInputChange('awards', 'All');
       } else {
         const currentAwards = formData.awards || '';
         const awardList = currentAwards ? currentAwards.split(',').map(a => a.trim()).filter(a => a && a !== 'All') : [];
         
         if (!awardList.includes(playerName)) {
           const newAwardList = [...awardList, playerName];
           handleInputChange('awards', newAwardList.join(', '));
         }
       }
       // Keep dropdown open for multi-select
       setShowAwardDropdown(true);
     } else if (fieldName === 'ageRange') {
       // Handle age range selection
       if (playerName === 'All') {
         handleInputChange('ageRange', 'All');
       } else {
         const currentAgeRanges = formData.ageRange || '';
         const ageRangeList = currentAgeRanges ? currentAgeRanges.split(',').map(a => a.trim()).filter(a => a && a !== 'All') : [];
         
         if (!ageRangeList.includes(playerName)) {
           const newAgeRangeList = [...ageRangeList, playerName];
           handleInputChange('ageRange', newAgeRangeList.join(', '));
         }
       }
       // Keep dropdown open for multi-select
       setShowAgeRangeDropdown(true);
     } else if (fieldName === 'teamsFilter') {
       // Handle teams filter selection
       if (playerName === 'All') {
         handleInputChange('teamsFilter', 'All');
       } else {
         const currentTeamsFilter = formData.teamsFilter || '';
         const teamsFilterList = currentTeamsFilter ? currentTeamsFilter.split(',').map(t => t.trim()).filter(t => t && t !== 'All') : [];
         
         if (!teamsFilterList.includes(playerName)) {
           const newTeamsFilterList = [...teamsFilterList, playerName];
           handleInputChange('teamsFilter', newTeamsFilterList.join(', '));
         }
       }
       // Keep dropdown open for multi-select
       setShowTeamsFilterDropdown(true);
     } else {
       // Handle players selection
       const currentPlayers = formData.players || '';
       const playerList = currentPlayers ? currentPlayers.split(',').map(p => p.trim()).filter(p => p) : [];
       
       if (!playerList.includes(playerName)) {
         const newPlayerList = [...playerList, playerName];
         handleInputChange('players', newPlayerList.join(', '));
       }
     }
    
         // Clear search term but keep dropdown open for multi-select
     if (fieldName === 'years') {
       setYearSearchTerm('');
       // Keep dropdown open for multi-select
     } else if (fieldName === 'positions') {
       setPositionSearchTerm('');
       // Keep dropdown open for multi-select
     } else if (fieldName === 'awards') {
       setAwardSearchTerm('');
       // Keep dropdown open for multi-select
     } else if (fieldName === 'ageRange') {
       setAgeRangeSearchTerm('');
       // Keep dropdown open for multi-select
     } else if (fieldName === 'teamsFilter') {
       setTeamsFilterSearchTerm('');
       // Keep dropdown open for multi-select
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
    } else if (fieldName === 'awards') {
      const currentAwards = formData.awards || '';
      const awardList = currentAwards.split(',').map(a => a.trim()).filter(a => a !== playerToRemove);
      handleInputChange('awards', awardList.join(', '));
         } else if (fieldName === 'ageRange') {
       const currentAgeRanges = formData.ageRange || '';
       const ageRangeList = currentAgeRanges.split(',').map(a => a.trim()).filter(a => a !== playerToRemove);
       handleInputChange('ageRange', ageRangeList.join(', '));
     } else if (fieldName === 'teamsFilter') {
       const currentTeamsFilter = formData.teamsFilter || '';
       const teamsFilterList = currentTeamsFilter.split(',').map(t => t.trim()).filter(t => t !== playerToRemove);
       handleInputChange('teamsFilter', teamsFilterList.join(', '));
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
      if (processedData.awards && typeof processedData.awards === 'string') {
        processedData.awards = processedData.awards.split(',').map(a => a.trim()).filter(a => a);
      }
             if (processedData.ageRange && typeof processedData.ageRange === 'string') {
         processedData.ageRange = processedData.ageRange.split(',').map(a => a.trim()).filter(a => a);
       }
       if (processedData.teamsFilter && typeof processedData.teamsFilter === 'string') {
         processedData.teamsFilter = processedData.teamsFilter.split(',').map(t => t.trim()).filter(t => t);
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
      
      // Debug: Check years being submitted
      console.log('Years being submitted:', processedData.years);
      console.log('Teams filter being submitted:', processedData.teamsFilter);
      
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
         if (field.name === 'players' || field.name === 'years' || field.name === 'positions' || field.name === 'awards' || field.name === 'ageRange' || field.name === 'teamsFilter') {
          return (
            <div className="multiselect-container">
              <div className="search-container">
                <input
                   type="text"
                                       placeholder={
                      field.name === 'years' ? "Search years..." : 
                      field.name === 'positions' ? "Search positions..." : 
                      field.name === 'awards' ? "Search awards..." :
                      field.name === 'ageRange' ? "Search ages..." :
                      field.name === 'teamsFilter' ? "Search teams..." :
                      "Search players..."
                    }
                                       value={
                      field.name === 'years' ? yearSearchTerm :
                      field.name === 'positions' ? positionSearchTerm :
                      field.name === 'awards' ? awardSearchTerm :
                      field.name === 'ageRange' ? ageRangeSearchTerm :
                      field.name === 'teamsFilter' ? teamsFilterSearchTerm :
                      playerSearchTerm
                    }
                   onChange={(e) => handlePlayerSearch(e.target.value, field.name)}
                                       onFocus={() => {
                      // Show dropdown with all options when field is focused
                      if (field.name === 'years') {
                        setFilteredYears(options.years || []);
                        setShowYearDropdown(true);
                      } else if (field.name === 'positions') {
                        setFilteredPositions(options.positions || []);
                        setShowPositionDropdown(true);
                      } else if (field.name === 'awards') {
                        setFilteredAwards(options.awards || []);
                        setShowAwardDropdown(true);
                                             } else if (field.name === 'ageRange') {
                         setFilteredAgeRanges(options.ageRanges || []);
                         setShowAgeRangeDropdown(true);
                       } else if (field.name === 'teamsFilter') {
                         setFilteredTeamsFilter(options.teamsFilter || []);
                         setShowTeamsFilterDropdown(true);
                       } else {
                         setFilteredPlayers(options.players || []);
                         setShowPlayerDropdown(true);
                       }
                    }}
                                     onBlur={(e) => {
                     // Delay hiding dropdown to allow clicking on options
                     setTimeout(() => {
                       // Check if the newly focused element is within the same dropdown
                       const currentContainer = e.target.closest('.multiselect-container');
                       const newFocus = document.activeElement;
                       const newContainer = newFocus ? newFocus.closest('.multiselect-container') : null;
                       
                       // Close dropdown if focus moved completely outside any dropdown container
                       if (!newContainer || newContainer !== currentContainer) {
                         if (field.name === 'years') {
                           setShowYearDropdown(false);
                         } else if (field.name === 'positions') {
                           setShowPositionDropdown(false);
                         } else if (field.name === 'awards') {
                           setShowAwardDropdown(false);
                         } else if (field.name === 'ageRange') {
                           setShowAgeRangeDropdown(false);
                         } else if (field.name === 'teamsFilter') {
                           setShowTeamsFilterDropdown(false);
                         } else {
                           setShowPlayerDropdown(false);
                         }
                       }
                     }, 150);
                   }}
                />
                                 {(field.name === 'years' ? showYearDropdown : 
                   field.name === 'positions' ? showPositionDropdown : 
                   field.name === 'awards' ? showAwardDropdown :
                   field.name === 'ageRange' ? showAgeRangeDropdown :
                   field.name === 'teamsFilter' ? showTeamsFilterDropdown :
                   showPlayerDropdown) && (
                                     <div className="player-dropdown">
                                            {(field.name === 'years' ? filteredYears : 
                         field.name === 'positions' ? filteredPositions : 
                         field.name === 'awards' ? filteredAwards :
                         field.name === 'ageRange' ? filteredAgeRanges :
                         field.name === 'teamsFilter' ? filteredTeamsFilter :
                         filteredPlayers).map((item, index) => {
                          const currentValue = formData[field.name] || '';
                          const selectedItems = currentValue ? currentValue.split(',').map(i => i.trim()).filter(i => i) : [];
                          const isSelected = selectedItems.includes(item.toString());
                          
                          return (
                            <div
                              key={index}
                              className={`player-option ${isSelected ? 'selected' : ''}`}
                              onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                addPlayer(item, field.name);
                              }}
                              onMouseDown={(e) => e.preventDefault()}
                            >
                              {item}
                            </div>
                          );
                        })}
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

             case 'range':
         if (field.name === 'years') {
           return (
             <div className="range-slider-container">
               <div className="range-slider">
                 <input
                   type="range"
                   min="1950"
                   max="2025"
                   value={seasonRange[0]}
                   onChange={(e) => {
                     const newMin = parseInt(e.target.value);
                     const newMax = Math.max(newMin, seasonRange[1]);
                     setSeasonRange([newMin, newMax]);
                     // Generate years array from range
                     const years = [];
                     for (let year = newMin; year <= newMax; year++) {
                       years.push(year);
                     }
                     handleInputChange('years', years.join(', '));
                   }}
                   className="range-slider-thumb min-thumb"
                 />
                 <input
                   type="range"
                   min="1950"
                   max="2025"
                   value={seasonRange[1]}
                   onChange={(e) => {
                     const newMax = parseInt(e.target.value);
                     const newMin = Math.min(seasonRange[0], newMax);
                     setSeasonRange([newMin, newMax]);
                     // Generate years array from range
                     const years = [];
                     for (let year = newMin; year <= newMax; year++) {
                       years.push(year);
                     }
                     handleInputChange('years', years.join(', '));
                   }}
                   className="range-slider-thumb max-thumb"
                 />
               </div>
               <div className="range-labels">
                 <span>{seasonRange[0]}</span>
                 <span>{seasonRange[1]}</span>
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
        <div className={`form-grid ${template.id === 'player_comparison' ? 'player-comparison-grid' : ''}`}>
          {template.fields && template.fields.map((field) => (
            <div key={field.name} className={`form-field ${field.name}`}>
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

