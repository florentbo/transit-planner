import type { TransportLine } from '../../entities/dashboard'
import { DeparturePill } from './DeparturePill'

const modeColors: Record<string, { bg: string; text: string; dot: string }> = {
  METRO: { bg: 'bg-orange-50 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-300', dot: 'bg-orange-500' },
  TRAM: { bg: 'bg-green-50 dark:bg-green-900/30', text: 'text-green-700 dark:text-green-300', dot: 'bg-green-500' },
  BUS: { bg: 'bg-teal-50 dark:bg-teal-900/30', text: 'text-teal-700 dark:text-teal-300', dot: 'bg-teal-500' },
  TRAIN: { bg: 'bg-purple-50 dark:bg-purple-900/30', text: 'text-purple-700 dark:text-purple-300', dot: 'bg-purple-500' },
}

const modeLabels: Record<string, string> = {
  METRO: 'M',
  TRAM: 'T',
  BUS: 'Bus',
  TRAIN: 'Train',
}

export function LineDepartures({ line }: { line: TransportLine }) {
  const colors = modeColors[line.mode] ?? modeColors.METRO
  const label = modeLabels[line.mode] ?? line.mode

  return (
    <div className="flex items-center gap-3 flex-wrap">
      <span
        className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-bold ${colors.bg} ${colors.text}`}
      >
        <span className={`h-2 w-2 rounded-full ${colors.dot}`} aria-hidden="true" />
        {label}{line.name}
      </span>

      <span className="text-xs text-gray-400 dark:text-gray-500">
        → {line.direction}
      </span>

      <div className="flex items-center gap-2" role="list" aria-label={`Departures for line ${line.name}`}>
        {line.departures.map((dep, i) => (
          <DeparturePill key={`${dep.minutesUntil}-${dep.destination}`} minutesUntil={dep.minutesUntil} variant={i === 0 ? 'next' : 'future'} />
        ))}
      </div>
    </div>
  )
}
