import type { RouteDepartures } from '../../entities/dashboard'
import { DeparturePill } from './DeparturePill'

export function LineDepartures({ route }: { route: RouteDepartures }) {
  return (
    <div className="flex items-center gap-3 flex-wrap">
      <span className="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-bold bg-orange-50 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300">
        <span className="h-2 w-2 rounded-full bg-orange-500" aria-hidden="true" />
        {route.lineNumber}
      </span>

      <span className="text-xs text-gray-400 dark:text-gray-500">
        {route.stopName} → {route.direction}
      </span>

      <div className="flex items-center gap-2" role="list" aria-label={`Departures for line ${route.lineNumber}`}>
        {route.departures.length > 0 ? (
          route.departures.map((dep, i) => (
            <DeparturePill key={`${dep.minutesUntilArrival}-${dep.destination}`} minutesUntil={dep.minutesUntilArrival} variant={i === 0 ? 'next' : 'future'} />
          ))
        ) : (
          <span className="text-xs text-gray-400 italic">No upcoming departures</span>
        )}
      </div>
    </div>
  )
}
