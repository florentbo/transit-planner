import { getGreeting, getFormattedDate } from '../utils/greeting'

export function GreetingSection({ userName }: { userName: string }) {
  return (
    <section aria-label="Greeting">
      <h2 className="text-2xl font-bold text-balance text-gray-900 dark:text-white sm:text-3xl">
        {getGreeting()}, {userName}
      </h2>
      <p className="mt-1 text-gray-500 dark:text-gray-400">{getFormattedDate()}</p>
    </section>
  )
}
