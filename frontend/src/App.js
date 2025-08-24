import React, { useState, useEffect } from 'react';
import './App.css';
import TemplateSelector from './components/TemplateSelector';
import GraphForm from './components/GraphForm';
import GraphDisplay from './components/GraphDisplay';
import { TEMPLATES } from './constants';
import { API_ENDPOINTS } from './config/api';

function App() {
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [graphData, setGraphData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Use hardcoded templates instead of API call
  useEffect(() => {
    setTemplates(Object.values(TEMPLATES));
  }, []);

  const handleTemplateSelect = (template) => {
    setSelectedTemplate(template);
    setGraphData(null);
    setError(null);
  };

  const handleGenerateGraph = async (formData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(API_ENDPOINTS.GENERATE_GRAPH, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Received graph data from backend:', data);
      setGraphData(data);
      setError(null);
    } catch (err) {
      console.error('Failed to generate graph:', err);
      setError('Failed to generate graph. Please try again.');
      setGraphData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleBackToTemplates = () => {
    setSelectedTemplate(null);
    setGraphData(null);
    setError(null);
  };

  return (
    <div className="App">
      <header className="App-header">
        <a 
          href="https://github.com/jackeyemean/nba-graph-maker" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="github-link"
        >
          <svg 
            stroke="currentColor" 
            fill="none" 
            strokeWidth="2" 
            viewBox="0 0 24 24" 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            className="github-icon"
            height="1em" 
            width="1em" 
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path>
          </svg>
          <span>jackeyemean/nba-graph-maker</span>
        </a>
      </header>
      
      <main className="App-main">
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}
        
        {!selectedTemplate ? (
          <TemplateSelector 
            templates={templates} 
            onTemplateSelect={handleTemplateSelect}
            loading={loading}
          />
        ) : (
          <div className="graph-creator">
            <button 
              className="back-button"
              onClick={handleBackToTemplates}
            >
              ← Back to Templates
            </button>
            
            <GraphForm 
              template={selectedTemplate}
              onGenerateGraph={handleGenerateGraph}
              loading={loading}
            />
            
            {graphData && (
              <GraphDisplay 
                graphData={graphData}
                template={selectedTemplate}
              />
            )}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
