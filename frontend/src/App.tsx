import "./App.css";
import { useState, useEffect } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from "recharts";
interface DeviceData {
  deviceId: string;
  voltage: number;
  std: number;
  variationPercent: number;
}

interface DevicesPayload {
  devices: DeviceData[];
}

export function App() {
  const [chartData, setChartData] = useState<Record<string, { time: string; voltage: number }[]>>({});
  const getCurrentTime = () => {
    const now = new Date();
    return `${now.getHours().toString().padStart(2, "0")}:${now
      .getMinutes()
      .toString()
      .padStart(2, "0")}:${now.getSeconds().toString().padStart(2, "0")}`;
  };

useEffect(() => {
  const eventSource = new EventSource(
    "http://localhost:8080/coordinator/stream"
  );

  eventSource.onmessage = (event) => {
    const payload: DevicesPayload = JSON.parse(event.data);
    console.log(payload);

    setChartData((prevData) => {
      const newData = { ...prevData };

      payload.devices.forEach((device) => {
        const current = [...(newData[device.deviceId] || [])];
        if (current.length >= 10) current.shift(); // manter histórico de 10

        current.push({
          time: getCurrentTime(),
          voltage: Number(device.voltage), // garante que é número
        });

        newData[device.deviceId] = current;
      });

      return newData;
    });
  };

  return () => eventSource.close();
}, []);
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
