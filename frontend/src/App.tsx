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
  deviceName:string;
  voltage: number;
  std: number;
  variationPercent: number;
  timestamp: string;
}

interface DevicesPayload {
  devices: DeviceData[];
}

export function App() {
  const [chartData, setChartData] = useState<
    Record<string, { time: string; voltage: number; deviceName: string }[]>
  >({});

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const res = await fetch("http://localhost:8080/coordinator/history");

        if (!res.ok) {
          throw new Error("HTTP error " + res.status);
        }

        const payload: Record<string, DeviceData[]> = await res.json();

        const initialData: Record<string, { time: string; voltage: number; deviceName: string }[]> =
          {};

        Object.keys(payload).forEach((deviceId) => {
          const history = payload[deviceId];

          initialData[deviceId] = history.map((entry) => ({
            time: new Date(entry.timestamp).toLocaleTimeString("pt-BR", {
              hour12: false,
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            }),
            voltage: entry.voltage,
            deviceName: entry.deviceName
          }));
        });

        setChartData(initialData);
        console.log(initialData);
        
      } catch (err) {
        console.error("Failed to load initial data", err);
      }
    };

    fetchInitialData();
  }, []);

  useEffect(() => {
    const eventSource = new EventSource(
      "http://localhost:8080/coordinator/stream"
    );

    eventSource.onmessage = (event) => {
      const payload: DevicesPayload = JSON.parse(event.data);

      setChartData((prevData) => {
        const updated = { ...prevData };

        payload.devices.forEach((device) => {
          const current = [...(updated[device.deviceId] || [])];

          if (current.length >= 10) current.shift();

          current.push({
            time: new Date(device.timestamp).toLocaleTimeString("pt-BR", {
              hour12: false,
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            }),
            voltage: Number(device.voltage),
            deviceName: device.deviceName
          });

          updated[device.deviceId] = current;
        });

        return updated;
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
        {Object.keys(chartData).map((chartKey) => {
          const data = chartData[chartKey];
          return (
            <div key={chartKey} className="chart-card">
              <div className="chart-header">
                <h3 className="chart-title">{data[data.length - 1].voltage}</h3>
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
