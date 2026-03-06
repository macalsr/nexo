import { httpClient } from './httpClient'

export type HealthResponse = {
  status: string
  service: string
  timestamp: string
}

export function getHealth(signal?: AbortSignal) {
  return httpClient.get<HealthResponse>('/health', { signal })
}
