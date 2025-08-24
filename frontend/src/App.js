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
        <h1>NBA Graph Generator</h1>
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
