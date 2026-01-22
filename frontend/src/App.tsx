import { useState } from 'react';
import { CreateRouteForm } from './components/CreateRouteForm';
import { RouteList } from './components/RouteList';
import './App.css';

function App() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleRouteCreated = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  return (
    <div className="App">
      <header>
        <h1>🚇 Transit Planner</h1>
        <p>Save and manage your favorite transit routes</p>
      </header>

      <main style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
        <CreateRouteForm onRouteCreated={handleRouteCreated} />
        <hr style={{ margin: '2rem 0' }} />
        <RouteList refreshTrigger={refreshTrigger} />
      </main>
    </div>
  );
}

export default App;
