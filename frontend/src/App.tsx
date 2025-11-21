import "./App.css";
import { useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from "recharts";

export function App() {
  const getCurrentTime = () => {
    const now = new Date();
    return `${now.getHours().toString().padStart(2, "0")}:${now
      .getMinutes()
      .toString()
      .padStart(2, "0")}:${now.getSeconds().toString().padStart(2, "0")}`;
  };
  const [chartData, setChartData] = useState<
    Record<string, { time: string; voltage: number }[]>
  >({
    chart1: [],
    chart2: [],
    chart3: [],
    chart4: [],
    chart5: [],
  });
  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1 className="dashboard-title">GridGuard</h1>
          <p className="dashboard-subtitle">
            Sistema de Monitoramento de Tensão em Tempo Real
          </p>
        </div>
        <div className="status-indicator">
          <div className="status-dot"></div>
          <span>Atualização em tempo real</span>
        </div>
      </header>

      <div className="charts-grid">
        {Object.keys(chartData).map((chartKey, index) => {
          const data = chartData[chartKey];
          return (
            <div key={chartKey} className="chart-card">
              <div className="chart-header">
                <h3 className="chart-title">{`Sensor ${index + 1}`}</h3>
                <div className="chart-stats">
                  <span className="stat-label">Atual:</span>
                  <span className="stat-value">
                    {data.length > 0 &&
                    typeof data[data.length - 1].voltage === "number"
                      ? `${data[data.length - 1].voltage.toFixed(2)}V`
                      : "--"}
                  </span>
                </div>
              </div>

              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={data}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2a3f5f" />
                  <XAxis
                    dataKey="time"
                    stroke="#8892a6"
                    style={{ fontSize: "12px" }}
                  />
                  <YAxis
                    domain={[200, 240]}
                    stroke="#8892a6"
                    style={{ fontSize: "12px" }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#1a2332",
                      border: "1px solid #2e5aff",
                      borderRadius: "8px",
                      color: "#ffffff",
                    }}
                  />
                  <Line
                    type="monotone"
                    dataKey="voltage"
                    stroke="#2e5aff"
                    strokeWidth={2}
                    dot={{ r: 3 }}
                    activeDot={{ r: 5 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default App;
