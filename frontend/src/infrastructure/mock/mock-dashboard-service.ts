import type { IDashboardService } from '../../use-cases/ports/dashboard-service'
import type { DashboardData } from '../../entities/dashboard'

const mockData: DashboardData = {
  userName: 'Florent',
  city: 'Brussels',
  favoriteRoute: {
    id: '1',
    name: 'Home → Work',
    origin: 'Merode',
    destination: 'Schuman',
    lines: [
      {
        id: 'stib-m1',
        name: '1',
        mode: 'METRO',
        boardingStop: 'Merode',
        alightingStop: 'Schuman',
        direction: 'Stockel',
        departures: [
          { minutesUntil: 2, destination: 'Stockel' },
          { minutesUntil: 5, destination: 'Stockel' },
          { minutesUntil: 12, destination: 'Stockel' },
        ],
      },
      {
        id: 'stib-m5',
        name: '5',
        mode: 'METRO',
        boardingStop: 'Merode',
        alightingStop: 'Schuman',
        direction: 'Erasme',
        departures: [
          { minutesUntil: 3, destination: 'Erasme' },
          { minutesUntil: 8, destination: 'Erasme' },
          { minutesUntil: 15, destination: 'Erasme' },
        ],
      },
    ],
  },
}

export const mockDashboardService: IDashboardService = {
  getDashboard: () => mockData,
}
