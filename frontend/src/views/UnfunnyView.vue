<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="unfunny">
    <header class="unfunny-header">
      <h1>Not funny, dad!</h1>
      <span class="user">{{ authStore.username }}</span>
      <button type="button" class="logout" @click="logout">Log out</button>
    </header>

    <div class="content">
      <iframe
        class="video"
        src="https://www.youtube.com/embed/dQw4w9WgXcQ"
        title="YouTube video"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
      />
      <p class="back-link">
        <RouterLink to="/">Back to todo list</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.unfunny {
  max-width: 32rem;
  margin: 0 auto;
  padding: 1rem;
}

.unfunny-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.unfunny-header h1 {
  margin: 0;
  flex: 1;
}

.user {
  font-size: 0.9rem;
  color: var(--color-text-muted, #666);
}

.logout {
  padding: 0.25rem 0.5rem;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.video {
  aspect-ratio: 16 / 9;
  width: 100%;
  border: none;
}

.back-link {
  margin: 0;
}
</style>
