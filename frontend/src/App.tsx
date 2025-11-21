import "./App.css";

export function App() {
  const getCurrentTime = () => {
  const now = new Date();
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
};

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
