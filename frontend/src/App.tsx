import { ServiceProvider } from './infrastructure/di/service-provider'
import { apiDashboardService } from './infrastructure/api/api-dashboard-service'
import { DashboardPage } from './presentation/pages/DashboardPage'

function App() {
  return (
    <ServiceProvider dashboardService={apiDashboardService}>
      <DashboardPage />
    </ServiceProvider>
  )
}

export default App
