import type { IDashboardService } from '../../use-cases/ports/dashboard-service'
import type { DeparturesData } from '../../entities/dashboard'

const API_BASE_URL = 'http://localhost:8080'

export const apiDashboardService: IDashboardService = {
  getDepartures: async (): Promise<DeparturesData> => {
    const response = await fetch(`${API_BASE_URL}/api/departures`)
    if (!response.ok) {
      throw new Error(`Failed to fetch departures: ${response.status}`)
    }
    return response.json()
  },
}
