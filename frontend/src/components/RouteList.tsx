import { useListRoutes } from '../gen/hooks/useListRoutes';

export function RouteList() {
  const { data: routes, isLoading, error } = useListRoutes();

  if (isLoading) {
    return (
      <div className="text-center py-8 text-gray-500 dark:text-gray-400">
        Loading routes...
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded-md">
        Failed to load routes. Please try again.
      </div>
    );
  }

  if (!routes || routes.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500 dark:text-gray-400">
        No routes yet. Create your first route above!
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
        Your Saved Routes ({routes.length})
      </h2>
      <div className="grid gap-4">
        {routes.map((route) => (
          <div
            key={route.id}
            className="bg-white dark:bg-gray-800 rounded-lg shadow p-4 border border-gray-200 dark:border-gray-700"
          >
            <h3 className="font-semibold text-gray-900 dark:text-white">
              {route.name}
            </h3>
            <p className="mt-1 text-gray-600 dark:text-gray-400">
              <span className="font-medium">{route.origin}</span>
              <span className="mx-2">→</span>
              <span className="font-medium">{route.destination}</span>
            </p>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-500">
              Created: {new Date(route.createdAt).toLocaleString()}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
