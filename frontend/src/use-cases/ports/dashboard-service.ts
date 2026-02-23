import type { DashboardData } from '../../entities/dashboard'

export interface IDashboardService {
  getDashboard(): DashboardData
}
