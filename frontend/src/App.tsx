import { ServiceProvider } from './infrastructure/di/service-provider'
import { mockDashboardService } from './infrastructure/mock/mock-dashboard-service'
import { DashboardPage } from './presentation/pages/DashboardPage'

function App() {
  return (
    <ServiceProvider dashboardService={mockDashboardService}>
      <DashboardPage />
    </ServiceProvider>
  )
}

export default App
