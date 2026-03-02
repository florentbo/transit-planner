import type { DeparturesData } from '../../entities/dashboard'

export interface IDashboardService {
  getDepartures(): Promise<DeparturesData>
}
