import { useQuery } from '@tanstack/react-query'
import { useDashboardService } from '../../infrastructure/di/use-dashboard-service'
import type { DeparturesData } from '../../entities/dashboard'

export function useDashboard() {
  const service = useDashboardService()

  return useQuery<DeparturesData, Error>({
    queryKey: ['departures'],
    queryFn: () => service.getDepartures(),
    refetchInterval: 30_000,
  })
}
