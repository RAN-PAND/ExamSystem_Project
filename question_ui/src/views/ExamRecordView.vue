<template>
  <div class="exam-record-page">
    <el-card shadow="never">
      <div class="record-header">
        <div>
          <span class="label">学生ID：</span>
          <el-input
            v-model="queryStudentId"
            placeholder="学生ID"
            style="width: 160px"
            :disabled="user.role === 'student'"
          />
          <el-button type="primary" size="small" @click="loadRecords" style="margin-left: 8px">
            查询
          </el-button>
        </div>
      </div>

      <el-table
        :data="records"
        v-loading="loading"
        style="width: 100%; margin-top: 16px;"
        size="small"
      >
        <el-table-column prop="paperName" label="试卷名称" min-width="180" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="submitTime" label="提交时间" min-width="180" />
      </el-table>

      <el-empty
        v-if="!loading && records.length === 0"
        description="暂无考试记录"
        style="margin-top: 16px;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const user = JSON.parse(localStorage.getItem('user') || '{}')

const records = ref([])
const loading = ref(false)

// 查询条件：学生ID
const queryStudentId = ref(user.role === 'student' ? user.id : '')

const loadRecords = async () => {
  if (!queryStudentId.value) {
    ElMessage.warning('请填写学生ID')
    return
  }
  loading.value = true
  try {
    const resp = await axios.get('http://localhost:9090/api/exam/record/my', {
      params: { studentId: queryStudentId.value }
    })
    records.value = resp.data.data || []
  } catch (e) {
    ElMessage.error('加载考试记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 学生登录时，自动加载自己的记录
  if (user.role === 'student' && user.id) {
    loadRecords()
  }
})
</script>

<style scoped>
.exam-record-page {
  padding: 16px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  margin-right: 8px;
}
</style>
