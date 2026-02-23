import type { ReactNode } from 'react'
import type { IDashboardService } from '../../use-cases/ports/dashboard-service'
import { ServiceContext } from './service-context'

export function ServiceProvider({
  dashboardService,
  children,
}: {
  dashboardService: IDashboardService
  children: ReactNode
}) {
  return <ServiceContext value={dashboardService}>{children}</ServiceContext>
}
