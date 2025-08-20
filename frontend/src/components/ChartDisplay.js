import React, { useEffect, useRef } from 'react';

const ChartDisplay = ({ chartData }) => {
  const chartRef = useRef(null);
  const chartInstance = useRef(null);

  useEffect(() => {
    const loadChartJS = async () => {
      if (!window.Chart) {
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
        script.onload = () => renderChart();
        document.head.appendChild(script);
      } else {
        renderChart();
      }
    };

    loadChartJS();

    return () => {
      if (chartInstance.current) {
        chartInstance.current.destroy();
      }
    };
  }, [chartData]);

  const renderChart = () => {
    if (!window.Chart || !chartRef.current) return;

    if (chartInstance.current) {
      chartInstance.current.destroy();
    }

    try {
      const config = typeof chartData === 'string' ? JSON.parse(chartData) : chartData;
      chartInstance.current = new window.Chart(chartRef.current, config);
    } catch (err) {
      console.error('Failed to render chart:', err);
    }
  };

  return (
    <div className="chart-display">
      <div className="chart-container">
        <canvas ref={chartRef}></canvas>
      </div>
    </div>
  );
};

export default ChartDisplay;
