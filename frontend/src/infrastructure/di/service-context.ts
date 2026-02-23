import { createContext } from 'react'
import type { IDashboardService } from '../../use-cases/ports/dashboard-service'

export const ServiceContext = createContext<IDashboardService | null>(null)
