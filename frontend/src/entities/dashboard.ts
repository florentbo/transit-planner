export type Departure = {
  minutesUntilArrival: number
  destination: string
}

export type RouteDepartures = {
  stopName: string
  lineNumber: string
  direction: string
  departures: Departure[]
}

export type DeparturesData = {
  lastUpdated: string
  routes: RouteDepartures[]
}
