import { useState, useEffect } from 'react'

const STALENESS_THRESHOLD_MS = 2 * 60 * 1000 // 2 minutes

export function StalenessWarning({ dataUpdatedAt }: { dataUpdatedAt: number }) {
  const [isStale, setIsStale] = useState(false)

  useEffect(() => {
    const check = () => {
      setIsStale(Date.now() - dataUpdatedAt > STALENESS_THRESHOLD_MS)
    }
    check()
    const interval = setInterval(check, 10_000) // check every 10s
    return () => clearInterval(interval)
  }, [dataUpdatedAt])

  if (!isStale) return null

  return (
    <p className="mt-2 text-xs text-amber-600 dark:text-amber-400" role="status">
      Data may be outdated
    </p>
  )
}
