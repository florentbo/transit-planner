import { CreateRouteForm } from './components/CreateRouteForm';
import { RouteList } from './components/RouteList';

function App() {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <header className="bg-white dark:bg-gray-800 shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
            Transit Planner
          </h1>
          <p className="mt-1 text-gray-600 dark:text-gray-400">
            Save and manage your favorite transit routes
          </p>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        <CreateRouteForm />
        <hr className="my-8 border-gray-200 dark:border-gray-700" />
        <RouteList />
      </main>
    </div>
  );
}

export default App;
