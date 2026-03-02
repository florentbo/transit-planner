export function ErrorCard({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="rounded-2xl border-l-4 border-l-red-400 bg-white p-5 shadow-sm dark:border-l-red-500 dark:bg-gray-800">
      <p className="text-gray-700 dark:text-gray-300">
        Couldn't load departures right now.
      </p>
      <button
        onClick={onRetry}
        className="mt-3 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 dark:focus:ring-offset-gray-800"
      >
        Try again
      </button>
    </div>
  )
}
