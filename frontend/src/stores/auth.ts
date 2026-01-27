import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const username = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(newToken: string, newUsername: string) {
    token.value = newToken
    username.value = newUsername
  }

  function clearAuth() {
    token.value = null
    username.value = null
  }

  return { token, username, isAuthenticated, setAuth, clearAuth }
})
