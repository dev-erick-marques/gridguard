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
  deviceName: string;
  voltage: number;
  std: number;
  variationPercent: number;
  timestamp: string;
  isShutdown:boolean
}

interface DevicesPayload {
  devices: DeviceData[];
}

export function App() {
  const [chartData, setChartData] = useState<
    Record<
      string,
      {
        time: string;
        voltage: number;
        std: number;
        variationPercent: number;
        deviceName: string;
        isShutdown:boolean;
      }[]
    >
  >({});
  const [metric, setMetric] = useState<"voltage" | "std" | "variationPercent">(
    "voltage"
  );

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const res = await fetch("http://localhost:8080/coordinator/history");

        if (!res.ok) {
          throw new Error("HTTP error " + res.status);
        }

        const payload: Record<string, DeviceData[]> = await res.json();

        const initialData: Record<
          string,
          {
            time: string;
            voltage: number;
            std: number;
            variationPercent: number;
            deviceName: string;
            isShutdown:boolean
          }[]
        > = {};

        Object.keys(payload).forEach((deviceId) => {
          const history = payload[deviceId];

          initialData[deviceId] = history.map((entry) => ({
            time: new Date(entry.timestamp).toLocaleTimeString("pt-BR", {
              hour12: false,
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
            }),
            std: entry.std,
            variationPercent: entry.variationPercent,
            voltage: entry.voltage,
            deviceName: entry.deviceName,
            isShutdown:entry.isShutdown
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
            deviceName: device.deviceName,
            std: device.std,
            variationPercent: device.variationPercent,
            isShutdown: device.isShutdown
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
            Real-Time Voltage Monitoring System
          </p>
        </div>
        <div className="status-indicator">
          <div className="status-dot"></div>
          <span>Live Updates</span>
        </div>
      </header>
      <div className="metric-selector">
        <label>
          <input
            type="radio"
            name="metric"
            value="voltage"
            checked={metric === "voltage"}
            onChange={() => setMetric("voltage")}
          />
          Voltage
        </label>
        <label>
          <input
            type="radio"
            name="metric"
            value="std"
            checked={metric === "std"}
            onChange={() => setMetric("std")}
          />
          Standart deviation
        </label>
        <label>
          <input
            type="radio"
            name="metric"
            value="variationPercent"
            checked={metric === "variationPercent"}
            onChange={() => setMetric("variationPercent")}
          />
          Variation
        </label>
      </div>

      <div className="charts-grid">
        {Object.keys(chartData).map((chartKey, index) => {
          const data = chartData[chartKey];
          return (
            <div key={chartKey} className="chart-card">
              <div className="chart-header">
                <h3 className="chart-title">
                  {"device-"+ (index + 1) +"  --  "}{data[data.length - 1].isShutdown?"Safe shutdown":"Normal operation"}
                </h3>
                <div className="chart-stats">
                  <span className="stat-label">Atual:</span>
                  <span className="stat-value">
                    {data.length > 0 &&
                    typeof data[data.length - 1][metric] === "number"
                      ? `${data[data.length - 1][metric].toFixed(2)}${
                          metric === "voltage"
                            ? "V"
                            : metric === "variationPercent"
                            ? "%"
                            : ""
                        }`
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
                    domain={
                      metric === "voltage"
                        ? [200, 240]
                        : metric === "std"
                        ? [0, 10]
                        : [0, 5]
                    }
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
                    dataKey={metric}
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
