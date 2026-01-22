import { useState, FormEvent } from 'react';
import { RoutesApi } from '../api/generated';
import { apiConfig } from '../api/config';

interface CreateRouteFormProps {
  onRouteCreated: () => void;
}

export function CreateRouteForm({ onRouteCreated }: CreateRouteFormProps) {
  const [name, setName] = useState('');
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      const api = new RoutesApi(apiConfig);
      await api.createRoute({ name, origin, destination });

      // Reset form
      setName('');
      setOrigin('');
      setDestination('');

      // Notify parent
      onRouteCreated();
    } catch (err) {
      setError('Failed to create route. Please try again.');
      console.error(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '2rem' }}>
      <h2>Create New Route</h2>

      {error && <div style={{ color: 'red', marginBottom: '1rem' }}>{error}</div>}

      <div style={{ marginBottom: '1rem' }}>
        <label>
          Route Name:
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g., Home to Work"
            required
            style={{ marginLeft: '0.5rem', padding: '0.5rem', width: '100%', maxWidth: '300px' }}
          />
        </label>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label>
          Origin:
          <input
            type="text"
            value={origin}
            onChange={(e) => setOrigin(e.target.value)}
            placeholder="e.g., Brussels Central"
            required
            style={{ marginLeft: '0.5rem', padding: '0.5rem', width: '100%', maxWidth: '300px' }}
          />
        </label>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label>
          Destination:
          <input
            type="text"
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            placeholder="e.g., European Parliament"
            required
            style={{ marginLeft: '0.5rem', padding: '0.5rem', width: '100%', maxWidth: '300px' }}
          />
        </label>
      </div>

      <button
        type="submit"
        disabled={isSubmitting}
        style={{ padding: '0.5rem 1rem', cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
      >
        {isSubmitting ? 'Creating...' : 'Create Route'}
      </button>
    </form>
  );
}
