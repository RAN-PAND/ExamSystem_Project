<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 这里是路由出口，路由器匹配到 LoginView 就会显示在这里 -->
  <RouterView />
</template>

<style>
/* 简单的全局重置，去掉浏览器默认的白边 */
body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}
#app {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  max-width: none; /* 覆盖 Vue 默认的最大宽度限制 */
}
</style>