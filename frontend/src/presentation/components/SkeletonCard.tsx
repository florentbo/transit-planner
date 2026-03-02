export function SkeletonCard() {
  return (
    <div
      className="rounded-2xl border-l-4 border-l-gray-300 bg-white p-5 shadow-sm dark:border-l-gray-600 dark:bg-gray-800"
      aria-busy="true"
      aria-label="Loading departures"
    >
      <div className="animate-pulse">
        <div className="h-5 w-32 rounded bg-gray-200 dark:bg-gray-700" />
        <div className="mt-4 space-y-3">
          {[1, 2].map(i => (
            <div key={i} className="flex items-center gap-3">
              <div className="h-6 w-10 rounded-full bg-gray-200 dark:bg-gray-700" />
              <div className="h-4 w-24 rounded bg-gray-200 dark:bg-gray-700" />
              <div className="flex gap-2">
                <div className="h-7 w-14 rounded-full bg-gray-200 dark:bg-gray-700" />
                <div className="h-7 w-14 rounded-full bg-gray-200 dark:bg-gray-700" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
