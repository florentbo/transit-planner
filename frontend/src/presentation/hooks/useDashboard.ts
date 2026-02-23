import { useMemo } from 'react'
import { useDashboardService } from '../../infrastructure/di/use-dashboard-service'
import { GetDashboard } from '../../use-cases/dashboard/get-dashboard'

export function useDashboard() {
  const service = useDashboardService()
  const useCase = useMemo(() => new GetDashboard(service), [service])
  return useCase.execute()
}
