import type { RouteDepartures } from '../../entities/dashboard'
import { LineDepartures } from './LineDepartures'

export function FavoriteRouteCard({ routes }: { routes: RouteDepartures[] }) {
  return (
    <article className="rounded-2xl border-l-4 border-l-orange-500 bg-white p-5 shadow-sm shadow-gray-200/50 dark:border-l-orange-400 dark:bg-gray-800 dark:shadow-none">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Your Commute</h3>
        </div>
      </div>

      <div className="mt-4 space-y-3">
        {routes.map(route => (
          <LineDepartures key={`${route.stopName}-${route.lineNumber}`} route={route} />
        ))}
      </div>
    </article>
  )
}
