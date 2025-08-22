import React from 'react';
import './TemplateSelector.css';
import { TEMPLATES } from '../constants';

const TemplateSelector = ({ templates, onTemplateSelect, loading }) => {
  const handleTemplateClick = (template) => {
    if (!loading) {
      onTemplateSelect(template);
    }
  };

  return (
    <div className="template-selector">
      <div className="templates-grid">
        {templates
          .sort((a, b) => {
            // Put player_comparison first
            if (a.id === 'player_comparison') return -1;
            if (b.id === 'player_comparison') return 1;
            return 0;
          })
          .map((template) => {
            // Map template names to simplified versions
            let displayName = template.name;
            if (template.id === 'player_comparison') {
              displayName = 'Compare Players';
            } else if (template.id === 'season_distribution') {
              displayName = 'Histogram';
            } else if (template.id === 'season_correlation') {
              displayName = 'Scatter Plot';
            }
            
            return (
              <div 
                key={template.id}
                className={`template-card ${loading ? 'disabled' : ''}`}
                onClick={() => handleTemplateClick(template)}
              >
                <h3>{displayName}</h3>
              </div>
            );
          })}
      </div>
      
      {loading && (
        <div className="loading-overlay">
          <div className="spinner"></div>
          <p>Loading templates...</p>
        </div>
      )}
    </div>
  );
};

export default TemplateSelector;

