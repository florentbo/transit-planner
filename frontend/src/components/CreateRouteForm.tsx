import { useState, type FormEvent } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useCreateRoute } from '../gen/hooks/useCreateRoute';
import { listRoutesQueryKey } from '../gen/hooks/useListRoutes';

export function CreateRouteForm() {
  const [name, setName] = useState('');
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');

  const queryClient = useQueryClient();
  const createRoute = useCreateRoute({
    mutation: {
      onSuccess: () => {
        setName('');
        setOrigin('');
        setDestination('');
        queryClient.invalidateQueries({ queryKey: listRoutesQueryKey() });
      },
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    createRoute.mutate({ data: { name, origin, destination } });
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
        Create New Route
      </h2>

      {createRoute.error && (
        <div className="mb-4 p-3 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded-md text-sm">
          Failed to create route. Please try again.
        </div>
      )}

      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Route Name
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g., Home to Work"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm
                       focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                       dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Origin
          </label>
          <input
            type="text"
            value={origin}
            onChange={(e) => setOrigin(e.target.value)}
            placeholder="e.g., Brussels Central"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm
                       focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                       dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Destination
          </label>
          <input
            type="text"
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            placeholder="e.g., European Parliament"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm
                       focus:ring-2 focus:ring-blue-500 focus:border-blue-500
                       dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
          />
        </div>

        <button
          type="submit"
          disabled={createRoute.isPending}
          className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400
                     text-white font-medium rounded-md shadow-sm
                     focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2
                     disabled:cursor-not-allowed transition-colors"
        >
          {createRoute.isPending ? 'Creating...' : 'Create Route'}
        </button>
      </div>
    </form>
  );
}
