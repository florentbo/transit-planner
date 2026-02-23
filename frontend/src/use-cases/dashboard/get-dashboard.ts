import type { IDashboardService } from '../ports/dashboard-service'
import type { DashboardData } from '../../entities/dashboard'

export class GetDashboard {
  private readonly dashboardService: IDashboardService

  constructor(dashboardService: IDashboardService) {
    this.dashboardService = dashboardService
  }

  execute(): DashboardData {
    return this.dashboardService.getDashboard()
  }
}
