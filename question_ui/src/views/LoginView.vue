<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧装饰区 -->
      <div class="left-side">
        <h2>数据库原理题库系统</h2>
        <p>专业 · 智能 · 高效</p>
      </div>
      
      <!-- 右侧表单区 -->
      <div class="right-side">
        <h3>{{ isLogin ? '用户登录' : '用户注册' }}</h3>
        
        <!-- ⚠️注意1：这里必须绑定 :model="form" -->
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
          
          <!-- 身份选择 (仅注册显示) -->
          <el-form-item prop="role" v-if="!isLogin" style="margin-bottom: 25px;">
            <el-radio-group v-model="form.role">
              <el-radio label="student">学生</el-radio>
              <el-radio label="teacher">教师</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 学号/工号 -->
          <!-- ⚠️注意2：prop 必须是 "userNum" -->
          <el-form-item prop="userNum">
            <!-- ⚠️注意3：v-model 必须是 "form.userNum" (千万别写成 username) -->
            <el-input 
              v-model="form.userNum" 
              placeholder="请输入学号/工号" 
              :prefix-icon="User" 
              size="large"
            />
          </el-form-item>

          <!-- 真实姓名 (仅注册显示) -->
          <el-form-item v-if="!isLogin" prop="realName">
            <el-input 
              v-model="form.realName" 
              placeholder="请输入真实姓名" 
              :prefix-icon="Postcard" 
              size="large"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password 
              size="large"
            />
          </el-form-item>

          <el-button type="primary" class="submit-btn" size="large" @click="handleSubmit">
            {{ isLogin ? '登 录' : '注 册' }}
          </el-button>

          <div class="toggle-box">
            <span @click="toggleMode">{{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Postcard } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const isLogin = ref(true)
const formRef = ref(null)
const router = useRouter()

// ⚠️注意4：定义里必须是 userNum
const form = reactive({
  userNum: '', 
  password: '',
  realName: '',
  role: 'student'
})

// 动态验证规则 (方案3核心)
const rules = computed(() => {
  const baseRules = {
    // ⚠️注意5：规则里必须是 userNum
    userNum: [{ required: true, message: '请输入学号或工号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  const registerRules = {
    role: [{ required: true, message: '请选择身份', trigger: 'change' }],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
  }
  return isLogin.value ? baseRules : { ...baseRules, ...registerRules }
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  if (formRef.value) {
    formRef.value.resetFields()
    formRef.value.clearValidate()
  }
  if(!isLogin.value) form.role = 'student'
}

const handleSubmit = () => {
  if (!formRef.value) return
  
  formRef.value.validate((valid) => {
    if (valid) {
      if (isLogin.value) {
        // 登录请求
        request.post('/api/login', form).then(res => {
          ElMessage.success('登录成功')
          localStorage.setItem('user', JSON.stringify(res.data))
          router.push('/home') 
        })
      } else {
        // 注册请求
        request.post('/api/register', form).then(res => {
          ElMessage.success('注册成功，请登录')
          isLogin.value = true
        })
      }
    } else {
      ElMessage.error('请填写完整信息')
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #108ee9 0%, #87d068 100%);
}
.login-box {
  width: 850px; height: 500px; background: white; border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3); display: flex; overflow: hidden;
}
.left-side {
  width: 45%; background-color: #3b5998; color: white;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
}
.right-side {
  width: 55%; padding: 40px; display: flex; flex-direction: column; justify-content: center;
}
.submit-btn { width: 100%; margin-top: 10px; }
.toggle-box { text-align: center; margin-top: 20px; font-size: 14px; color: #666; cursor: pointer; }
</style>