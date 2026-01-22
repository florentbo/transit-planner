import { useState, useEffect } from 'react';
import { RoutesApi, SavedRouteResponse } from '../api/generated';
import { apiConfig } from '../api/config';

interface RouteListProps {
  refreshTrigger: number;
}

export function RouteList({ refreshTrigger }: RouteListProps) {
  const [routes, setRoutes] = useState<SavedRouteResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRoutes = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const api = new RoutesApi(apiConfig);
        const response = await api.listRoutes();
        setRoutes(response.data);
      } catch (err) {
        setError('Failed to load routes. Please try again.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRoutes();
  }, [refreshTrigger]);

  if (isLoading) {
    return <div>Loading routes...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  if (routes.length === 0) {
    return <div>No routes yet. Create your first route above!</div>;
  }

  return (
    <div>
      <h2>Your Saved Routes ({routes.length})</h2>
      <div style={{ display: 'grid', gap: '1rem' }}>
        {routes.map((route) => (
          <div
            key={route.id}
            style={{
              border: '1px solid #ccc',
              borderRadius: '8px',
              padding: '1rem',
              backgroundColor: '#f9f9f9'
            }}
          >
            <h3>{route.name}</h3>
            <p>
              <strong>{route.origin}</strong> → <strong>{route.destination}</strong>
            </p>
            <small style={{ color: '#666' }}>
              Created: {new Date(route.createdAt).toLocaleString()}
            </small>
          </div>
        ))}
      </div>
    </div>
  );
}
