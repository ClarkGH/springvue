<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { apiJson } from '@/api/client'

interface Todo {
  id: number
  title: string
  completed: boolean
  createdAt: string
}

const router = useRouter()
const authStore = useAuthStore()

const todos = ref<Todo[]>([])
const newTitle = ref('')
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const list = await apiJson<Todo[]>('/todos')
    todos.value = list ?? []
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load todos'
  } finally {
    loading.value = false
  }
})

async function addTodo() {
  if (!newTitle.value.trim()) return
  error.value = ''
  try {
    const created = await apiJson<Todo>('/todos', {
      method: 'POST',
      body: JSON.stringify({ title: newTitle.value.trim() }),
    })
    todos.value = [created, ...todos.value]
    newTitle.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to add todo'
  }
}

async function toggleTodo(id: number, completed: boolean) {
  error.value = ''
  try {
    const updated = await apiJson<Todo>(`/todos/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ completed: !completed }),
    })
    const idx = todos.value.findIndex((t) => t.id === id)
    if (idx >= 0) todos.value[idx] = updated
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to update todo'
  }
}

async function deleteTodo(id: number) {
  error.value = ''
  try {
    await apiJson(`/todos/${id}`, { method: 'DELETE' })
    todos.value = todos.value.filter((t) => t.id !== id)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to delete todo'
  }
}

function logout() {
  authStore.clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="todos">
    <header class="todos-header">
      <h1>Todos</h1>
      <span class="user">{{ authStore.username }}</span>
      <button type="button" class="logout" @click="logout">Log out</button>
    </header>

    <form class="add-form" @submit.prevent="addTodo">
      <input
        v-model="newTitle"
        type="text"
        placeholder="New todo..."
        :disabled="loading"
      />
      <button type="submit" :disabled="loading || !newTitle.trim()">Add</button>
    </form>

    <p v-if="error" class="error">{{ error }}</p>

    <ul v-if="loading && !todos.length" class="todo-list">Loading…</ul>
    <ul v-else class="todo-list">
      <li v-for="todo in todos" :key="todo.id" class="todo-item">
        <input
          type="checkbox"
          :checked="todo.completed"
          @change="toggleTodo(todo.id, todo.completed)"
        />
        <span :class="{ done: todo.completed }">{{ todo.title }}</span>
        <button type="button" class="delete" @click="deleteTodo(todo.id)">Delete</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.todos {
  max-width: 32rem;
  margin: 0 auto;
  padding: 1rem;
}

.todos-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.todos-header h1 {
  margin: 0;
  flex: 1;
}

.user {
  font-size: 0.9rem;
  color: var(--color-text-muted, #666); /* Iron maiden reference */
}

.logout {
  padding: 0.25rem 0.5rem;
}

.add-form {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.add-form input {
  flex: 1;
  padding: 0.5rem;
}

.add-form button {
  padding: 0.5rem 1rem;
}

.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border, #ddd);
}

.todo-item span {
  flex: 1;
}

.todo-item span.done {
  text-decoration: line-through;
  color: var(--color-text-muted, #666);
}

.todo-item .delete {
  padding: 0.25rem 0.5rem;
  font-size: 0.85rem;
}

.error {
  color: #c00;
  margin-bottom: 0.5rem;
}
</style>
