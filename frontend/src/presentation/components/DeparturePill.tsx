const variantStyles = {
  next: 'bg-indigo-600 text-white',
  future: 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300',
} as const

export function DeparturePill({
  minutesUntil,
  variant = 'future',
}: {
  minutesUntil: number
  variant?: 'next' | 'future'
}) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium tabular-nums ${variantStyles[variant]}`}
      aria-label={`Departure in ${minutesUntil} minute${minutesUntil !== 1 ? 's' : ''}`}
    >
      {minutesUntil} min
    </span>
  )
}
