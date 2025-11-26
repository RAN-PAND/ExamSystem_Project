<template>
  <el-card>
    <!-- 顶部搜索 + 操作按钮 -->
    <div class="header-box">
      <div class="search">
        <el-input
          v-model="searchName"
          placeholder="按试卷名称搜索..."
          style="width: 200px; margin-right: 10px;"
          clearable
          @clear="loadPapers"
          @keyup.enter.native="loadPapers"
        />
        <el-button type="primary" @click="loadPapers">查询</el-button>
      </div>

      <div class="actions">
        <el-button type="success" @click="openDialog(null)">+ 新增试卷</el-button>
      </div>
    </div>

    <!-- 试卷列表表格 -->
    <el-table :data="filteredPapers" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="试卷名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
      <el-table-column prop="questionCount" label="题目数" width="90" />
      <el-table-column prop="totalScore" label="总分" width="90" />

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="previewPaper(scope.row.id)">预览</el-button>
          <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑试卷弹窗 -->
    <el-dialog v-model="dialogVisible" :title="paperForm.id ? '编辑试卷' : '新增试卷'" width="900px">
      <el-form :model="paperForm" label-width="100px">
        <el-form-item label="试卷名称">
          <el-input v-model="paperForm.name" />
        </el-form-item>
        <el-form-item label="试卷说明">
          <el-input v-model="paperForm.description" type="textarea" rows="2" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">选择题目</el-divider>

      <!-- 题库池：从题库接口加载的一批题目 -->
      <el-table
        :data="questionPool"
        border
        height="220"
        @selection-change="handleQuestionSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="题目ID" width="80" />
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
      </el-table>

      <div style="margin: 10px 0; text-align: right;">
        <el-button size="small" type="primary" @click="addSelectedToPaper">添加到试卷</el-button>
      </div>

      <!-- 已选题目列表 -->
      <el-table :data="selectedQuestions" border size="small" style="margin-top: 10px;">
        <el-table-column prop="orderNo" label="顺序" width="70" />
        <el-table-column prop="content" label="题干" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="120">
          <template #default="scope">
            <el-input-number v-model="scope.row.score" :min="0" :max="100" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="text" size="small" @click="removeQuestion(scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="savePaper">保 存</el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览弹窗 -->
    <el-dialog v-model="previewVisible" title="试卷预览" width="800px">
      <div v-if="previewPaperData">
        <h3>{{ previewPaperData.name }}</h3>
        <p style="color: #666;">{{ previewPaperData.description }}</p>
        <p>总分：{{ previewPaperData.totalScore }} 分，题目数：{{ previewPaperData.questionCount }}</p>

        <el-divider />

        <div v-for="(q, index) in previewPaperData.questions" :key="q.id" style="margin-bottom: 16px;">
          <div>{{ index + 1 }}. {{ q.content }}</div>
          <div style="margin-left: 16px; margin-top: 4px;">
            <div>A. {{ q.optionA }}</div>
            <div>B. {{ q.optionB }}</div>
            <div>C. {{ q.optionC }}</div>
            <div>D. {{ q.optionD }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const searchName = ref('')

// 如果后端未来支持分页，可以用 total/pageSize/currentPage
const total = ref(0)
const pageSize = ref(10)

const dialogVisible = ref(false)

const paperForm = reactive({
  id: null,
  name: '',
  description: '',
  totalScore: 0,
  questionCount: 0
})

// 题库池 & 勾选
const questionPool = ref([])
const selectedFromPool = ref([])

// 当前试卷中已选题目
const selectedQuestions = ref([]) // { questionId, content, score, orderNo }

// 预览相关
const previewVisible = ref(false)
const previewPaperData = ref(null)

// 根据搜索名称进行简单前端过滤
const filteredPapers = computed(() => {
  if (!searchName.value) {
    return tableData.value
  }
  return tableData.value.filter(p =>
    p.name && p.name.toLowerCase().includes(searchName.value.toLowerCase())
  )
})

// 加载试卷列表
const loadPapers = () => {
  request.get('/api/paper/list').then(res => {
    tableData.value = res.data || []
    total.value = tableData.value.length
  })
}

// 加载题库池（简单版：取前 50 条题目）
const loadQuestionPool = () => {
  request.get('/api/question/list', {
    params: {
      pageNum: 1,
      pageSize: 50
    }
  }).then(res => {
    questionPool.value = res.data.list || []
  })
}

const handleQuestionSelectionChange = (selection) => {
  selectedFromPool.value = selection
}

const addSelectedToPaper = () => {
  if (!selectedFromPool.value.length) {
    ElMessage.warning('请先在上方题目列表中勾选题目')
    return
  }
  const existsIds = selectedQuestions.value.map(i => i.questionId)
  selectedFromPool.value.forEach(q => {
    if (!existsIds.includes(q.id)) {
      selectedQuestions.value.push({
        questionId: q.id,
        content: q.content,
        score: 10,
        orderNo: selectedQuestions.value.length + 1
      })
    }
  })
}

const removeQuestion = (index) => {
  selectedQuestions.value.splice(index, 1)
  // 重新整理顺序
  selectedQuestions.value.forEach((item, idx) => {
    item.orderNo = idx + 1
  })
}

// 打开弹窗：新增/编辑
const openDialog = async (row) => {
  if (!row) {
    // 新增
    paperForm.id = null
    paperForm.name = ''
    paperForm.description = ''
    paperForm.totalScore = 0
    paperForm.questionCount = 0
    selectedQuestions.value = []
  } else {
    // 编辑：根据 ID 查询试卷详情
    const res = await request.get(`/api/paper/detail/${row.id}`)
    const data = res.data
    paperForm.id = data.id
    paperForm.name = data.name
    paperForm.description = data.description
    paperForm.totalScore = data.totalScore
    paperForm.questionCount = data.questionCount

    selectedQuestions.value = (data.questions || []).map((q, index) => ({
      questionId: q.id,
      content: q.content,
      score: 10, // 如后端返回 score 字段可替换此处
      orderNo: index + 1
    }))
  }

  dialogVisible.value = true
}

// 保存试卷
const savePaper = () => {
  if (!paperForm.name) {
    ElMessage.warning('请输入试卷名称')
    return
  }
  if (!selectedQuestions.value.length) {
    ElMessage.warning('请至少选择一道题目')
    return
  }

  let totalScore = 0
  selectedQuestions.value.forEach((item, index) => {
    totalScore += item.score || 0
    item.orderNo = index + 1
  })
  paperForm.totalScore = totalScore
  paperForm.questionCount = selectedQuestions.value.length

  const payload = {
    paper: paperForm,
    questionList: selectedQuestions.value.map(item => ({
      paperId: paperForm.id,
      questionId: item.questionId,
      score: item.score,
      orderNo: item.orderNo
    }))
  }

  request.post('/api/paper/save', payload).then(() => {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadPapers()
  })
}

// 删除试卷
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这份试卷吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/api/paper/delete/${id}`).then(() => {
      ElMessage.success('删除成功')
      loadPapers()
    })
  })
}

// 预览试卷
const previewPaper = async (id) => {
  const res = await request.get(`/api/paper/detail/${id}`)
  previewPaperData.value = res.data
  previewVisible.value = true
}

// 预留：如后端支持分页，可在此处理页码变化
const handlePageChange = () => {
  // TODO: 接入后端分页时实现
}

onMounted(() => {
  loadPapers()
  loadQuestionPool()
})
</script>

<style scoped>
.header-box {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.search {
  display: flex;
  align-items: center;
}
.actions {
  display: flex;
  align-items: center;
}
.actions > *:not(:last-child) {
  margin-right: 10px;
}
</style>