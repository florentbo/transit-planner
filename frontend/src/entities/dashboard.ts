export type TransportMode = 'METRO' | 'TRAM' | 'BUS' | 'TRAIN'

export type Departure = {
  minutesUntil: number
  destination: string
}

export type TransportLine = {
  id: string
  name: string
  mode: TransportMode
  boardingStop: string
  alightingStop: string
  direction: string
  departures: Departure[]
}

export type FavoriteRoute = {
  id: string
  name: string
  origin: string
  destination: string
  lines: TransportLine[]
}

export type DashboardData = {
  userName: string
  city: string
  favoriteRoute: FavoriteRoute
}
