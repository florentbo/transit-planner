import { useDashboard } from '../hooks/useDashboard'
import { useDarkMode } from '../hooks/useDarkMode'
import { DashboardHeader } from '../components/DashboardHeader'
import { GreetingSection } from '../components/GreetingSection'
import { FavoriteRouteCard } from '../components/FavoriteRouteCard'
import { DarkModeToggle } from '../components/DarkModeToggle'

export function DashboardPage() {
  const { data } = useDashboard()
  const { isDark, toggle } = useDarkMode()

  return (
    <div className="min-h-screen bg-stone-50 motion-safe:transition-colors dark:bg-gray-900">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:rounded focus:bg-indigo-600 focus:px-4 focus:py-2 focus:text-white">
        Skip to main content
      </a>

      <DashboardHeader city="Brussels" userInitial="F" />

      <main id="main" className="mx-auto max-w-2xl px-4 py-8">
        <GreetingSection userName="Florent" />

        <section aria-label="Departures" className="mt-8">
          {data && <FavoriteRouteCard routes={data.routes} />}
        </section>
      </main>

      <DarkModeToggle isDark={isDark} onToggle={toggle} />
    </div>
  )
}
