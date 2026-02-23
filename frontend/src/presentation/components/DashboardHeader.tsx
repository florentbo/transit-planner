export function DashboardHeader({
  city,
  userInitial,
}: {
  city: string
  userInitial: string
}) {
  return (
    <header className="bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-700 dark:to-purple-700">
      <div className="mx-auto flex max-w-2xl items-center justify-between px-4 py-4">
        <div className="flex items-center gap-3">
          <svg className="h-8 w-8 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" d="M8 7v8a2 2 0 002 2h4a2 2 0 002-2V7m-8 0V5a2 2 0 012-2h4a2 2 0 012 2v2m-8 0h8M6 21h12M8 17v4m8-4v4" />
          </svg>
          <span className="text-lg font-bold text-white">Transit Planner</span>
        </div>

        <div className="flex items-center gap-3">
          <span className="hidden rounded-full bg-white/15 px-3 py-1 text-sm font-medium text-white sm:inline-flex">
            {city}
          </span>
          <div
            className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-white/30 bg-white/20 text-sm font-bold text-white"
            role="img"
            aria-label={`User: ${userInitial}`}
          >
            {userInitial}
          </div>
        </div>
      </div>
    </header>
  )
}
