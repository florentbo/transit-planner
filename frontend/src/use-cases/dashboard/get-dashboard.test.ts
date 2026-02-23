import { describe, it, expect } from 'vitest'
import { GetDashboard } from './get-dashboard'
import type { IDashboardService } from '../ports/dashboard-service'
import type { DashboardData } from '../../entities/dashboard'

const mockDashboardData: DashboardData = {
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
    ],
  },
}

describe('GetDashboard', () => {
  it('returns dashboard data from the service', () => {
    const mockService: IDashboardService = {
      getDashboard: () => mockDashboardData,
    }
    const useCase = new GetDashboard(mockService)

    const result = useCase.execute()

    expect(result).toEqual(mockDashboardData)
  })

  it('returns the user name from the service', () => {
    const mockService: IDashboardService = {
      getDashboard: () => mockDashboardData,
    }
    const useCase = new GetDashboard(mockService)

    expect(useCase.execute().userName).toBe('Florent')
  })

  it('returns route with at least one transport line', () => {
    const mockService: IDashboardService = {
      getDashboard: () => mockDashboardData,
    }
    const useCase = new GetDashboard(mockService)

    const result = useCase.execute()

    expect(result.favoriteRoute.lines.length).toBeGreaterThan(0)
  })

  it('delegates to the injected service', () => {
    const customData: DashboardData = {
      ...mockDashboardData,
      userName: 'Alice',
      city: 'London',
    }
    const mockService: IDashboardService = {
      getDashboard: () => customData,
    }
    const useCase = new GetDashboard(mockService)

    expect(useCase.execute().userName).toBe('Alice')
    expect(useCase.execute().city).toBe('London')
  })
})
