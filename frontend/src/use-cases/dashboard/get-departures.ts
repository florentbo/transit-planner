import type { IDashboardService } from '../ports/dashboard-service'
import type { DeparturesData } from '../../entities/dashboard'

export class GetDepartures {
  private readonly dashboardService: IDashboardService

  constructor(dashboardService: IDashboardService) {
    this.dashboardService = dashboardService
  }

  execute(): Promise<DeparturesData> {
    return this.dashboardService.getDepartures()
  }
}
