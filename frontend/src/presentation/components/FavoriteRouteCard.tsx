import type { FavoriteRoute } from '../../entities/dashboard'
import { LineDepartures } from './LineDepartures'
import { StarIcon } from './StarIcon'

export function FavoriteRouteCard({ route }: { route: FavoriteRoute }) {
  return (
    <article
      className="rounded-2xl border-l-4 border-l-orange-500 bg-white p-5 shadow-sm shadow-gray-200/50 dark:border-l-orange-400 dark:bg-gray-800 dark:shadow-none"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <StarIcon />
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{route.name}</h3>
          </div>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            {route.origin} → {route.destination}
          </p>
        </div>
      </div>

      <div className="mt-4 space-y-3">
        {route.lines.map(line => (
          <LineDepartures key={line.id} line={line} />
        ))}
      </div>
    </article>
  )
}
