import type { IDashboardService } from '../ports/dashboard-service'
import type { DeparturesData } from '../../entities/dashboard'

export class GetDepartures {
  constructor(private readonly dashboardService: IDashboardService) {}

  execute(): Promise<DeparturesData> {
    return this.dashboardService.getDepartures()
  }
}
