import React, { useState } from 'react';
import './App.css';
import ChartDisplay from './components/ChartDisplay';
import ChatInterface from './components/ChatInterface';

function App() {
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGenerateChart = async (prompt) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('http://localhost:8080/api/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ prompt }),
      });

      const data = await response.json();
      
      if (data.error) {
        setError(data.error);
        setChartData(null);
      } else {
        setChartData(data.chartData);
        setError(null);
      }
    } catch (err) {
      setError('Failed to generate chart. Please try again.');
      setChartData(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>NBA Graph Generator</h1>
        <p>Generate NBA statistics visualizations using natural language</p>
      </header>
      
      <main className="App-main">
        <ChatInterface onGenerateChart={handleGenerateChart} loading={loading} />
        
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}
        
        {chartData && (
          <ChartDisplay chartData={chartData} />
        )}
      </main>
    </div>
  );
}

export default App;
