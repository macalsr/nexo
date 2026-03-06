const fallbackApiBaseUrl = 'http://localhost:8080'

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

export const env = {
  apiBaseUrl:
    configuredApiBaseUrl && configuredApiBaseUrl.length > 0
      ? configuredApiBaseUrl
      : fallbackApiBaseUrl,
} as const
