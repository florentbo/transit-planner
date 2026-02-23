import { useDashboard } from '../hooks/useDashboard'
import { useDarkMode } from '../hooks/useDarkMode'
import { DashboardHeader } from '../components/DashboardHeader'
import { GreetingSection } from '../components/GreetingSection'
import { FavoriteRouteCard } from '../components/FavoriteRouteCard'
import { DarkModeToggle } from '../components/DarkModeToggle'
import { StarIcon } from '../components/StarIcon'

export function DashboardPage() {
  const { userName, city, favoriteRoute } = useDashboard()
  const { isDark, toggle } = useDarkMode()

  return (
    <div className="min-h-screen bg-stone-50 motion-safe:transition-colors dark:bg-gray-900">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:rounded focus:bg-indigo-600 focus:px-4 focus:py-2 focus:text-white">
        Skip to main content
      </a>

      <DashboardHeader city={city} userInitial={userName[0]} />

      <main id="main" className="mx-auto max-w-2xl px-4 py-8">
        <GreetingSection userName={userName} />

        <section aria-label="Favorite route" className="mt-8">
          <div className="mb-4 flex items-center gap-2">
            <StarIcon />
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Your Favorite</h2>
          </div>
          <FavoriteRouteCard route={favoriteRoute} />
        </section>
      </main>

      <DarkModeToggle isDark={isDark} onToggle={toggle} />
    </div>
  )
}
