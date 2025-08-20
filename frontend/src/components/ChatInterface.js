import React, { useState } from 'react';

const ChatInterface = ({ onGenerateChart, loading }) => {
  const [prompt, setPrompt] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (prompt.trim() && !loading) {
      onGenerateChart(prompt);
    }
  };

  return (
    <div className="chat-interface">
      <form onSubmit={handleSubmit}>
        <textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Describe the chart you want to see... (e.g., 'Stephen Curry assists per season', 'Derrick Rose vs Kobe Bryant points', '2023 season points leaders')"
          rows={3}
          disabled={loading}
        />
        <button type="submit" disabled={loading || !prompt.trim()}>
          {loading ? 'Generating...' : 'Generate Chart'}
        </button>
      </form>
    </div>
  );
};

export default ChatInterface;
