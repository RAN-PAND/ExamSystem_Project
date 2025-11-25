<template>
  <div class="layout-container">
    <!-- 1. 左侧菜单栏 -->
    <div class="aside">
      <div class="logo">数据库题库系统</div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#304156"
        text-color="#fff"
        :default-active="$route.path"
        router
        class="el-menu-vertical"
      >
        <!-- 菜单项 -->
        <el-menu-item index="/home/question">
          <el-icon><List /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        
        <el-menu-item index="/home/knowledge">
          <el-icon><Collection /></el-icon>
          <span>知识点管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 2. 右侧主体区 -->
    <div class="main-content">
      <!-- 顶部 Header -->
      <div class="header">
        <div class="breadcrumb">
          当前用户：{{ user.realName }} 
          <el-tag size="small" effect="dark">{{ userRole }}</el-tag>
        </div>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>
      
      <!-- 核心：子页面路由出口 -->
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { List, Collection } from '@element-plus/icons-vue'

const router = useRouter()
// 从浏览器缓存获取用户信息
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 计算角色中文名
const userRole = computed(() => {
  if (user.role === 'admin') return '管理员'
  if (user.role === 'teacher') return '教师'
  return '学生'
})

// 退出登录
const logout = () => {
  localStorage.removeItem('user') // 清除缓存
  router.push('/') // 跳回登录页
}
</script>

<style scoped>
.layout-container { display: flex; height: 100vh; width: 100%; }
.aside { width: 220px; background-color: #304156; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 20px; font-weight: bold; border-bottom: 1px solid #1f2d3d; background-color: #2b3a4d;}
.el-menu-vertical { border-right: none; flex: 1; }
.main-content { flex: 1; display: flex; flex-direction: column; background-color: #f0f2f5; }
.header { height: 60px; background: white; padding: 0 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.content-body { padding: 20px; flex: 1; overflow-y: auto; }
</style>