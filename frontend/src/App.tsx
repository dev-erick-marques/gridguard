import "./App.css";

export function App() {
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
          <span>Real-Time Update</span>
        </div>
      </header>

      <div className="charts-grid"></div>
    </div>
  );
}

export default App;
