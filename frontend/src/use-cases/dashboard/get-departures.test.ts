import { describe, it, expect } from 'vitest'
import { GetDepartures } from './get-departures'
import type { IDashboardService } from '../ports/dashboard-service'
import type { DeparturesData } from '../../entities/dashboard'

const mockDeparturesData: DeparturesData = {
  lastUpdated: '2026-03-02T20:00:00Z',
  routes: [
    {
      stopName: 'Woest',
      lineNumber: '51',
      direction: 'Gare du Midi',
      departures: [
        { minutesUntilArrival: 3, destination: 'Gare du Midi' },
        { minutesUntilArrival: 7, destination: 'Gare du Midi' },
      ],
    },
  ],
}

describe('GetDepartures', () => {
  it('returns departures data from the service', async () => {
    const mockService: IDashboardService = {
      getDepartures: async () => mockDeparturesData,
    }
    const useCase = new GetDepartures(mockService)
    const result = await useCase.execute()
    expect(result).toEqual(mockDeparturesData)
  })

  it('delegates to the injected service', async () => {
    const customData: DeparturesData = {
      lastUpdated: '2026-03-02T21:00:00Z',
      routes: [],
    }
    const mockService: IDashboardService = {
      getDepartures: async () => customData,
    }
    const useCase = new GetDepartures(mockService)
    const result = await useCase.execute()
    expect(result.routes).toEqual([])
  })
})
