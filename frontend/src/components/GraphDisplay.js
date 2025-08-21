import React from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line, Bar, Scatter } from 'react-chartjs-2';
import './GraphDisplay.css';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const getDefaultColor = (index, alpha = 1) => {
  const colors = [
    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', 
    '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF'
  ];
  const color = colors[index % colors.length];
  if (alpha < 1) {
    return color + Math.floor(alpha * 255).toString(16).padStart(2, '0');
  }
  return color;
};

const GraphDisplay = ({ graphData, template }) => {
  if (!graphData) return null;

  const renderLineChart = () => {
    if (!graphData.datasets || graphData.datasets.length === 0) {
      return <div className="no-data">No data available for line chart</div>;
    }

    console.log('Rendering line chart with datasets:', graphData.datasets);

    // Calculate minimum X value across all datasets for proper axis scaling
    let minX = Infinity;
    graphData.datasets.forEach(dataset => {
      if (dataset.xValues && dataset.xValues.length > 0) {
        const datasetMinX = Math.min(...dataset.xValues);
        if (datasetMinX < minX) {
          minX = datasetMinX;
        }
      }
    });

    // For line graphs, each player should have their own independent X-axis progression
    // We'll use scatter plot data format but render as a line chart
    const chartData = {
      datasets: graphData.datasets.map((dataset, index) => ({
        label: dataset.label,
        data: dataset.xValues.map((x, i) => ({
          x: x,
          y: dataset.yValues[i]
        })),
        borderColor: dataset.borderColor || getDefaultColor(index),
        backgroundColor: dataset.backgroundColor || getDefaultColor(index, 0.1),
        fill: dataset.fill || false,
        tension: 0.1,
        pointRadius: 4,
        pointHoverRadius: 6,
        showLine: true, // This makes it a line chart
      })),
    };

    console.log('Chart.js data:', chartData);

    const options = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
        },
        title: {
          display: true,
          text: graphData.title || 'Line Chart',
        },
        tooltip: {
          mode: 'index',
          intersect: false,
        },
      },
             scales: {
         x: {
           display: true,
           title: {
             display: true,
             text: getStatLabel(graphData.metadata?.xAxisType) || 'X-Axis',
           },
           min: minX !== Infinity ? minX : undefined,
           grid: {
             display: true,
             drawOnChartArea: true,
           },
           ticks: {
             stepSize: 1, // Show every year
             callback: function(value) {
               // Format years as "XXXX" without comma
               if (graphData.metadata?.xAxisType === 'year') {
                 return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, "");
               }
               return value;
             }
           }
         },
         y: {
           display: true,
           title: {
             display: true,
             text: getStatLabel(graphData.metadata?.yAxisType) || 'Y-Axis',
           },
         },
       },
      interaction: {
        mode: 'nearest',
        axis: 'x',
        intersect: false,
      },
    };

    return <Scatter data={chartData} options={options} />;
  };

  const renderHistogram = () => {
    if (!graphData.binEdges || !graphData.binCounts) {
      return <div className="no-data">No data available for histogram</div>;
    }

    // Create more intuitive bin labels
    const labels = graphData.binEdges.slice(0, -1).map((edge, index) => {
      const nextEdge = graphData.binEdges[index + 1];
      if (index === 0) {
        return `≤ ${nextEdge.toFixed(1)}`;
      } else if (index === graphData.binEdges.length - 2) {
        return `> ${edge.toFixed(1)}`;
      } else {
        return `${edge.toFixed(1)} - ${nextEdge.toFixed(1)}`;
      }
    });

    const chartData = {
      labels,
      datasets: [
        {
          label: 'Number of Players',
          data: graphData.binCounts,
          backgroundColor: 'rgba(54, 162, 235, 0.6)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1,
        },
      ],
    };

    const options = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
        title: {
          display: true,
          text: graphData.title || 'Histogram',
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              const count = context.parsed.y;
              const labels = [`Count: ${count}`];
              
              // Show all player names with their values
              if (graphData.metadata && graphData.metadata.binPlayers) {
                const binIndex = context.dataIndex;
                const players = graphData.metadata.binPlayers[binIndex];
                if (players && players.length > 0) {
                  labels.push('Players:');
                  players.forEach(player => labels.push(`  ${player}`));
                }
              }
              
              return labels;
            },
          },
        },
      },
             scales: {
         x: {
           display: true,
           title: {
             display: true,
             text: getStatLabel(graphData.metadata?.stat) || 'Statistic Value',
           },
         },
         y: {
           display: true,
           title: {
             display: true,
             text: 'Players',
           },
           beginAtZero: true,
         },
       },
    };

    return <Bar data={chartData} options={options} />;
  };

  const renderScatterPlot = () => {
    if (!graphData.points || graphData.points.length === 0) {
      return <div className="no-data">No data available for scatter plot</div>;
    }

    console.log('Scatter plot - graphData:', graphData);
    console.log('Scatter plot - xAxisLabel:', graphData.xAxisLabel);
    console.log('Scatter plot - yAxisLabel:', graphData.yAxisLabel);

    const chartData = {
      datasets: [
        {
          label: 'Players',
          data: graphData.points.map(point => ({
            x: point.x,
            y: point.y,
            player: point.player,
            team: point.team,
            year: point.year,
            label: point.label,
          })),
          backgroundColor: 'rgba(54, 162, 235, 0.6)',
          borderColor: 'rgba(54, 162, 235, 1)',
          pointRadius: 6,
          pointHoverRadius: 8,
        },
      ],
    };

    const options = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
        title: {
          display: true,
          text: graphData.title || 'Scatter Plot',
        },
                                   tooltip: {
            callbacks: {
              label: (context) => {
                const point = context.raw;
                const labels = [`Player: ${point.player}`];
                if (point.team) labels.push(`Team: ${point.team}`);
                if (point.year) labels.push(`Season: ${point.year}`);
                return labels;
              },
            },
          },
      },
             scales: {
         x: {
           display: true,
           title: {
             display: true,
             text: getStatLabel(graphData.metadata?.xAxisStat) || 'X-Axis',
           },
         },
         y: {
           display: true,
           title: {
             display: true,
             text: getStatLabel(graphData.metadata?.yAxisStat) || 'Y-Axis',
           },
         },
       },
    };

    return <Scatter data={chartData} options={options} />;
  };

  const getDefaultColor = (index, alpha = 1) => {
    const colors = [
      `rgba(255, 99, 132, ${alpha})`,
      `rgba(54, 162, 235, ${alpha})`,
      `rgba(255, 206, 86, ${alpha})`,
      `rgba(75, 192, 192, ${alpha})`,
      `rgba(153, 102, 255, ${alpha})`,
      `rgba(255, 159, 64, ${alpha})`,
    ];
    return colors[index % colors.length];
  };

  const getStatLabel = (statName) => {
    if (!statName) return 'Statistic';
    
    switch (statName.toLowerCase()) {
      case 'age':
        return 'Age';
      case 'year':
      case 'season':
        return 'Season';
      case 'points':
      case 'ppg':
        return 'Points Per Game';
      case 'assists':
        return 'Assists Per Game';
      case 'rebounds':
        return 'Rebounds Per Game';
      case 'steals':
        return 'Steals Per Game';
      case 'blocks':
        return 'Blocks Per Game';
      case 'minutes_per_game':
      case 'mpg':
        return 'Minutes Per Game';
      case 'field_goal_percentage':
      case 'fg%':
        return 'Field Goal %';
      case 'three_point_percentage':
      case '3p%':
        return '3-Point %';
      case 'free_throw_percentage':
      case 'ft%':
        return 'Free Throw %';
      case 'turnovers':
        return 'Turnovers Per Game';
      case 'personal_fouls':
        return 'Personal Fouls Per Game';
      default:
        return statName;
    }
  };

  const renderChart = () => {
    switch (graphData.graphType) {
      case 'line':
        return renderLineChart();
      case 'histogram':
        return renderHistogram();
      case 'scatter':
        return renderScatterPlot();
      default:
        return <div className="no-data">Unsupported chart type: {graphData.graphType}</div>;
    }
  };

  return (
    <div className="graph-display">
      <div className="chart-container">
        {renderChart()}
      </div>
      
      {graphData.metadata && (
        <div className="chart-metadata">
          <h4>Chart Information</h4>
          <div className="metadata-grid">
            {Object.entries(graphData.metadata).map(([key, value]) => (
              <div key={key} className="metadata-item">
                <strong>{key}:</strong> {value}
              </div>
            ))}
          </div>
        </div>
      )}
      
      {graphData.sqlQuery && (
        <details className="sql-query">
          <summary>View SQL Query</summary>
          <pre>{graphData.sqlQuery}</pre>
        </details>
      )}
    </div>
  );
};

export default GraphDisplay;

