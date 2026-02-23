import { use } from 'react'
import { ServiceContext } from './service-context'
import type { IDashboardService } from '../../use-cases/ports/dashboard-service'

export function useDashboardService(): IDashboardService {
  const ctx = use(ServiceContext)
  if (!ctx) throw new Error('ServiceProvider missing: wrap your app in <ServiceProvider>')
  return ctx
}
