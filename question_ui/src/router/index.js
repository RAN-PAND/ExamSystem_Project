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
        },
        {
          path: 'paper',
          name: 'ExamPaper',
          component: () => import('../views/ExamPaperView.vue')
        },
        {
          path: 'exam-do',
          name: 'ExamDo',
          component: () => import('../views/ExamDoView.vue')
        },
        {
          path: 'exam-record',
          name: 'ExamRecord',
          component: () => import('../views/ExamRecordView.vue')
        }
      ]
    }
  ]
})

// 路由守卫：没登录不许进后台，并根据角色调整 /home 默认入口
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 如果要去 /home 开头的页面，且没登录
  if (to.path.startsWith('/home') && !user) {
    next('/')
    return
  }

  // 已登录用户访问 /home 根路径时，根据角色跳转到不同首页
  if (to.path === '/home' && user) {
    if (user.role === 'student') {
      next('/home/exam-do')
    } else {
      next('/home/question')
    }
    return
  }

  next()
})

export default router