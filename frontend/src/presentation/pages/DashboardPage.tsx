import { useDashboard } from '../hooks/useDashboard'
import { useDarkMode } from '../hooks/useDarkMode'
import { DashboardHeader } from '../components/DashboardHeader'
import { GreetingSection } from '../components/GreetingSection'
import { FavoriteRouteCard } from '../components/FavoriteRouteCard'
import { DarkModeToggle } from '../components/DarkModeToggle'
import { SkeletonCard } from '../components/SkeletonCard'
import { ErrorCard } from '../components/ErrorCard'
import { StalenessWarning } from '../components/StalenessWarning'

export function DashboardPage() {
  const { data, isLoading, error, dataUpdatedAt, refetch } = useDashboard()
  const { isDark, toggle } = useDarkMode()

  // Determine what to render in the departures section
  let content: React.ReactNode
  if (isLoading && !data) {
    // Initial load — show skeleton
    content = <SkeletonCard />
  } else if (error && !data) {
    // First load failed, no cached data — show error
    content = <ErrorCard onRetry={() => refetch()} />
  } else if (data) {
    // Have data (possibly stale if refresh failed)
    content = (
      <>
        {error && (
          <p className="mb-2 text-xs text-amber-600 dark:text-amber-400" role="status">
            Having trouble refreshing — showing last known data
          </p>
        )}
        <FavoriteRouteCard routes={data.routes} />
        <StalenessWarning dataUpdatedAt={dataUpdatedAt} />
      </>
    )
  }

  return (
    <div className="min-h-screen bg-stone-50 motion-safe:transition-colors dark:bg-gray-900">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:rounded focus:bg-indigo-600 focus:px-4 focus:py-2 focus:text-white">
        Skip to main content
      </a>

      <DashboardHeader city="Brussels" userInitial="F" />

      <main id="main" className="mx-auto max-w-2xl px-4 py-8">
        <GreetingSection userName="Florent" />

        <section aria-label="Departures" className="mt-8">
          {content}
        </section>
      </main>

      <DarkModeToggle isDark={isDark} onToggle={toggle} />
    </div>
  )
}
