const ACCESS_TOKEN_KEY = 'nexo.accessToken'

export function getAccessToken() {
  return (
    window.localStorage.getItem(ACCESS_TOKEN_KEY) ??
    window.sessionStorage.getItem(ACCESS_TOKEN_KEY)
  )
}

export function storeAccessToken(token: string, rememberMe: boolean) {
  clearAccessToken()

  const storage = rememberMe ? window.localStorage : window.sessionStorage
  storage.setItem(ACCESS_TOKEN_KEY, token)
}

export function clearAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  window.sessionStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function hasAccessToken() {
  return getAccessToken() !== null
}
