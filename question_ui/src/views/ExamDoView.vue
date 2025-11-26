<template>
  <div class="exam-do-page">
    <el-card class="exam-header" shadow="never">
      <div class="exam-header-top">
        <div>
          <span class="label">选择试卷：</span>
          <el-select v-model="selectedPaperId" placeholder="请选择试卷" @change="handlePaperChange" style="width: 260px">
            <el-option
              v-for="paper in paperOptions"
              :key="paper.id"
              :label="paper.name"
              :value="paper.id"
            />
          </el-select>
        </div>
        <div class="student-input">
          <span class="label">学生ID：</span>
          <el-input v-model="studentId" placeholder="请输入学生ID" style="width: 160px" />
        </div>
      </div>
      <div v-if="examPaper" class="exam-header-info">
        <div>试卷名称：{{ examPaper.paperName }}</div>
        <div>总分：{{ examPaper.totalScore }} 分，题目数：{{ examPaper.questionCount }} 题</div>
        <div v-if="examPaper.description">说明：{{ examPaper.description }}</div>
      </div>
    </el-card>

    <el-card class="exam-body" v-loading="loadingPaper" shadow="never">
      <div v-if="!examPaper">
        <el-empty description="请先选择一套试卷" />
      </div>

      <div v-else>
        <div
          v-for="(q, index) in examPaper.questions"
          :key="q.id"
          class="exam-question-item"
        >
          <div class="question-title">
            <span class="q-index">第 {{ index + 1 }} 题（{{ q.score }} 分）</span>
            <span class="q-content">{{ q.content }}</span>
          </div>

          <!-- 单选题（默认题型） -->
          <el-radio-group
            v-if="!q.questionType || q.questionType === 1"
            v-model="answers[q.id]"
            class="options-group"
          >
            <el-radio label="A">A. {{ q.optionA }}</el-radio>
            <el-radio label="B">B. {{ q.optionB }}</el-radio>
            <el-radio label="C">C. {{ q.optionC }}</el-radio>
            <el-radio label="D">D. {{ q.optionD }}</el-radio>
          </el-radio-group>

          <!-- 判断题：T/F，对应后端 T/F 编码 -->
          <el-radio-group
            v-else-if="q.questionType === 3"
            v-model="answers[q.id]"
            class="options-group"
          >
            <el-radio label="T">对</el-radio>
            <el-radio label="F">错</el-radio>
          </el-radio-group>

          <!-- 填空题：单空 -->
          <el-input
            v-else-if="q.questionType === 4"
            v-model="answers[q.id]"
            placeholder="请输入答案"
            clearable
          />

          <!-- 简答题：多行文本 -->
          <el-input
            v-else-if="q.questionType === 5"
            type="textarea"
            :rows="4"
            v-model="answers[q.id]"
            placeholder="请简要作答"
          />
        </div>

        <div class="exam-actions">
          <el-button type="primary" :disabled="submitting || !examPaper" @click="handleSubmit">
            提交试卷
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 提交结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="考试结果" width="600px">
      <div v-if="submitResult">
        <el-result
          icon="success"
          title="本次得分：" :sub-title="submitResult.totalScore + ' 分'"
        />

        <el-table :data="submitResult.details" size="small" style="width: 100%; margin-top: 10px;">
          <el-table-column label="题目ID" prop="questionId" width="90" />
          <el-table-column label="学生答案" prop="studentAnswer" width="100" />
          <el-table-column label="正确答案" prop="correctAnswer" width="100" />
          <el-table-column label="得分" prop="score" width="80" />
          <el-table-column label="是否正确" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.correct ? 'success' : 'danger'">
                {{ scope.row.correct ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const paperOptions = ref([]) // 下拉可选试卷列表
const selectedPaperId = ref(null)
const studentId = ref('1')   // 演示用，可手动修改

const examPaper = ref(null)         // 当前试卷考试视图
const answers = reactive({})        // questionId -> 答案

const loadingPaper = ref(false)
const submitting = ref(false)

const resultDialogVisible = ref(false)
const submitResult = ref(null)

// 1. 加载试卷列表（复用 /api/paper/list）
const loadPaperOptions = async () => {
  try {
    const resp = await axios.get('http://localhost:9090/api/paper/list')
    paperOptions.value = resp.data.data || []
  } catch (e) {
    ElMessage.error('加载试卷列表失败')
  }
}

// 2. 切换试卷时，加载考试视图
const handlePaperChange = async () => {
  if (!selectedPaperId.value) return
  loadingPaper.value = true
  examPaper.value = null
  Object.keys(answers).forEach(key => delete answers[key])
  try {
    const resp = await axios.get(`http://localhost:9090/api/exam/paper/${selectedPaperId.value}`)
    examPaper.value = resp.data.data
  } catch (e) {
    ElMessage.error('加载试卷详情失败')
  } finally {
    loadingPaper.value = false
  }
}

// 3. 提交试卷
const handleSubmit = async () => {
  if (!studentId.value) {
    ElMessage.warning('请填写学生ID')
    return
  }
  if (!examPaper.value) {
    ElMessage.warning('请先选择试卷')
    return
  }

  const answerItems = examPaper.value.questions.map(q => ({
    questionId: q.id,
    answer: answers[q.id] || ''
  }))

  const payload = {
    studentId: Number(studentId.value),
    paperId: examPaper.value.paperId,
    answers: answerItems
  }

  submitting.value = true
  try {
    const resp = await axios.post('http://localhost:9090/api/exam/submit', payload)
    submitResult.value = resp.data.data
    resultDialogVisible.value = true
    ElMessage.success('交卷成功')
  } catch (e) {
    ElMessage.error('交卷失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPaperOptions()
})
</script>

<style scoped>
.exam-do-page {
  padding: 16px;
}

.exam-header {
  margin-bottom: 16px;
}

.exam-header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-input {
  display: flex;
  align-items: center;
}

.label {
  margin-right: 8px;
}

.exam-header-info {
  margin-top: 8px;
  color: #666;
}

.exam-body {
  min-height: 300px;
}

.exam-question-item {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.question-title {
  margin-bottom: 8px;
}

.q-index {
  font-weight: 600;
  margin-right: 8px;
}

.options-group {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.options-group .el-radio {
  display: block;
  margin: 2px 0;
}

.exam-actions {
  margin-top: 16px;
  text-align: right;
}
</style>