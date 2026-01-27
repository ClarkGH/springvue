import { createRouter, createWebHistory } from 'vue-router'
import TodoListView from '../views/TodoListView.vue'
import LoginView from '../views/LoginView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'todos',
      component: TodoListView,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
  ],
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !useAuthStore().isAuthenticated) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router
