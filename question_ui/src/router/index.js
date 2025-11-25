import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import Layout from '../views/Layout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView
    },
    {
      path: '/home',
      name: 'Layout',
      component: Layout,
      redirect: '/home/question',
      children: [
        {
          path: 'question',
          name: 'Question',
          component: () => import('../views/QuestionView.vue')
        },
        {
          path: 'knowledge',  // ✅ 新增知识点路由
          name: 'Knowledge',
          component: () => import('../views/KnowledgeView.vue')
        }
      ]
    }
  ]
})

// 路由守卫：没登录不许进后台
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  // 如果要去 /home 开头的页面，且没登录
  if (to.path.startsWith('/home') && !user) {
    next('/') // 踢回登录页
  } else {
    next() // 放行
  }
})

export default router