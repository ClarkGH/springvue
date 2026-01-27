<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { apiJson } from '@/api/client'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = 'Username and password required'
    return
  }
  loading.value = true
  try {
    const res = await apiJson<{ token: string; username: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: username.value.trim(), password: password.value }),
    })
    authStore.setAuth(res.token, res.username)
    router.replace('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <h1>Login</h1>
    <form @submit.prevent="submit">
      <div>
        <label for="username">Username</label>
        <input id="username" v-model="username" type="text" autocomplete="username" />
      </div>
      <div>
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" autocomplete="current-password" />
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="loading">Log in</button>
    </form>
  </div>
</template>

<style scoped>
.login {
  max-width: 20rem;
  margin: 2rem auto;
  padding: 1rem;
}

.login h1 {
  margin-bottom: 1rem;
}

.login label {
  display: block;
  margin-bottom: 0.25rem;
}

.login input {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 0.75rem;
  box-sizing: border-box;
}

.login button {
  padding: 0.5rem 1rem;
}

.login .error {
  color: #c00;
  margin-bottom: 0.5rem;
}
</style>
