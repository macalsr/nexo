import { beforeEach, describe, expect, it } from 'vitest'
import { clearAccessToken, getAccessToken, replaceAccessToken, storeAccessToken } from './tokenStorage'

describe('tokenStorage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.sessionStorage.clear()
  })

  it('stores remembered sessions in localStorage', () => {
    storeAccessToken('remembered-token', true)

    expect(window.localStorage.getItem('nexo.accessToken')).toBe('remembered-token')
    expect(window.sessionStorage.getItem('nexo.accessToken')).toBeNull()
    expect(getAccessToken()).toBe('remembered-token')
  })

  it('stores session-only logins in sessionStorage', () => {
    storeAccessToken('session-token', false)

    expect(window.sessionStorage.getItem('nexo.accessToken')).toBe('session-token')
    expect(window.localStorage.getItem('nexo.accessToken')).toBeNull()
    expect(getAccessToken()).toBe('session-token')
  })

  it('clears tokens from both storages', () => {
    window.localStorage.setItem('nexo.accessToken', 'remembered-token')
    window.sessionStorage.setItem('nexo.accessToken', 'session-token')

    clearAccessToken()

    expect(window.localStorage.getItem('nexo.accessToken')).toBeNull()
    expect(window.sessionStorage.getItem('nexo.accessToken')).toBeNull()
    expect(getAccessToken()).toBeNull()
  })

  it('replaces token in localStorage when session is remembered', () => {
    storeAccessToken('old-token', true)

    replaceAccessToken('new-token')

    expect(window.localStorage.getItem('nexo.accessToken')).toBe('new-token')
    expect(window.sessionStorage.getItem('nexo.accessToken')).toBeNull()
  })

  it('replaces token in sessionStorage when session is not remembered', () => {
    storeAccessToken('old-token', false)

    replaceAccessToken('new-token')

    expect(window.sessionStorage.getItem('nexo.accessToken')).toBe('new-token')
    expect(window.localStorage.getItem('nexo.accessToken')).toBeNull()
  })
})
