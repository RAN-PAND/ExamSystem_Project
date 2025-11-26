<template>
  <el-card>
    <!-- P/D 指标解释 -->
    <el-alert
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 10px;"
    >
      <template #title>
        难度P：0~1，数值越小题目越简单；区分度D：-1~1，> 0.2 视为合格题目。
      </template>
    </el-alert>

    <!-- 1. 顶部搜索与操作栏 -->
    <div class="header-box">
      <div class="search">
        <el-input 
          v-model="searchContent" 
          placeholder="搜索题干内容..." 
          style="width: 200px; margin-right: 10px;" 
          clearable 
          @clear="loadData" 
        />
        
        <!-- 新增：知识点筛选 -->
        <el-select 
          v-model="searchKnowledgeId" 
          placeholder="选择知识点" 
          clearable 
          style="width: 150px; margin-right: 10px;"
          @change="loadData"
        >
          <el-option 
            v-for="item in knowledgeList" 
            :key="item.id" 
            :label="item.name" 
            :value="item.id" 
          />
        </el-select>

        <!-- 新增：难度筛选 -->
        <el-select 
          v-model="searchDifficulty" 
          placeholder="选择难度" 
          clearable 
          style="width: 120px; margin-right: 10px;"
          @change="loadData"
        >
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="较难" :value="3" />
          <el-option label="困难" :value="4" />
        </el-select>

        <!-- 新增：题型筛选 -->
        <el-select
          v-model="searchQuestionType"
          placeholder="选择题型"
          clearable
          style="width: 140px; margin-right: 10px;"
          @change="loadData"
        >
          <el-option label="单选题" :value="1" />
          <el-option label="判断题" :value="3" />
          <el-option label="填空题" :value="4" />
          <el-option label="简答题" :value="5" />
        </el-select>

        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      
      <div class="actions">
        <el-button
          type="primary"
          @click="handleExport"
        >
          导出 Excel
        </el-button>

        <el-upload
          :show-file-list="false"
          :http-request="handleImport"
          accept=".xls,.xlsx"
        >
          <el-button type="warning">导入 Excel</el-button>
        </el-upload>

        <el-button
          type="info"
          @click="handleRecalcStats"
        >
          刷新统计
        </el-button>

        <!-- 新增：批量删除按钮 -->
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button type="success" @click="openDialog(null)">+ 新增试题</el-button>
      </div>
    </div>

    <!-- 2. 数据表格 -->
    <el-table 
      :data="tableData" 
      border 
      stripe 
      style="width: 100%; margin-top: 20px;"
      @selection-change="handleSelectionChange"
    >
      <!-- 新增：多选列 -->
      <el-table-column type="selection" width="55" />
      
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="content" label="题干" show-overflow-tooltip min-width="200" />
      
      <!-- 新增：知识点列 -->
      <el-table-column label="知识点" width="150">
        <template #default="scope">
          {{ getKnowledgeName(scope.row.knowledgeId) }}
        </template>
      </el-table-column>

      <el-table-column prop="difficultyLevel" label="难度" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.difficultyLevel===1" type="success">简单</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel===2" type="primary">中等</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel===3" type="warning">较难</el-tag>
          <el-tag v-else type="danger">困难</el-tag>
        </template>
      </el-table-column>

      <!-- 优化：动态指标列，增加区分度警告 -->
      <el-table-column label="动态指标" width="200">
        <template #default="scope">
          <div>难度P: {{ scope.row.difficultyP }}</div>
          <div style="display: flex; align-items: center;">
            区分度D: {{ scope.row.discriminationD }}
            <!-- 区分度 < 0.2 显示红色警告 -->
            <el-icon 
              v-if="scope.row.discriminationD < 0.2 && scope.row.discriminationD > 0" 
              color="red" 
              style="margin-left: 5px;"
            >
              <WarningFilled />
            </el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 3. 分页组件 -->
    <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 4. 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="题干">
          <el-input v-model="form.content" type="textarea" rows="3" />
        </el-form-item>
        
        <!-- 新增：知识点选择 -->
        <el-form-item label="关联知识点">
          <el-select v-model="form.knowledgeId" placeholder="请选择知识点" style="width: 100%;">
            <el-option 
              v-for="item in knowledgeList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选项A"> <el-input v-model="form.optionA" /> </el-form-item>
        <el-form-item label="选项B"> <el-input v-model="form.optionB" /> </el-form-item>
        <el-form-item label="选项C"> <el-input v-model="form.optionC" /> </el-form-item>
        <el-form-item label="选项D"> <el-input v-model="form.optionD" /> </el-form-item>
        
        <el-form-item label="正确答案">
          <el-radio-group v-model="form.answer">
            <el-radio label="A">A</el-radio>
            <el-radio label="B">B</el-radio>
            <el-radio label="C">C</el-radio>
            <el-radio label="D">D</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="预设难度">
          <el-rate v-model="form.difficultyLevel" :max="4" show-score />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchContent = ref('')
const searchKnowledgeId = ref(null)    // 新增：知识点筛选
const searchDifficulty = ref(null)     // 新增：难度筛选
const searchQuestionType = ref(null)   // 新增：题型筛选
const dialogVisible = ref(false)
const knowledgeList = ref([])        // 新增：知识点列表
const selectedIds = ref([])          // 新增：选中的题目ID

const form = reactive({
  id: null, 
  content: '', 
  optionA: '', 
  optionB: '', 
  optionC: '', 
  optionD: '', 
  answer: 'A', 
  difficultyLevel: 1,
  knowledgeId: null  // 新增
})

// 新增：加载知识点列表
const loadKnowledgeList = () => {
  request.get('/api/knowledge/list').then(res => {
    knowledgeList.value = res.data
  })
}

// 新增：根据知识点ID获取名称
const getKnowledgeName = (knowledgeId) => {
  const knowledge = knowledgeList.value.find(k => k.id === knowledgeId)
  return knowledge ? knowledge.name : '未分类'
}

// 加载数据（增加筛选参数）
const loadData = () => {
  request.get('/api/question/list', {
    params: {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      content: searchContent.value,
      knowledgeId: searchKnowledgeId.value,      // 知识点筛选
      difficultyLevel: searchDifficulty.value,   // 难度筛选
      questionType: searchQuestionType.value     // 题型筛选
    }
  }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  })
}

// 翻页
const handlePageChange = (val) => {
  currentPage.value = val
  loadData()
}

// 打开弹窗
const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = null
    form.content = ''
    form.optionA = ''; form.optionB = ''; form.optionC = ''; form.optionD = ''
    form.answer = 'A'
    form.difficultyLevel = 1
    form.knowledgeId = null  // 新增
  }
  dialogVisible.value = true
}

// 保存
const save = () => {
  // 新增：验证知识点必选
  if (!form.knowledgeId) {
    ElMessage.warning('请选择关联知识点')
    return
  }

  if (form.id) {
    request.put('/api/question/update', form).then(() => {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      loadData()
    })
  } else {
    request.post('/api/question/add', form).then(() => {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      loadData()
    })
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这道题吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/api/question/delete/${id}`).then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

// 新增：处理多选变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 新增：批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 道题吗？`, '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/api/question/batchDelete', { data: selectedIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      loadData()
    })
  })
}

const handleImport = (options) => {
  const file = options.file
  if (!file) {
    ElMessage.warning('请选择要导入的 Excel 文件')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  request.post('/api/question/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(() => {
    ElMessage.success('导入成功')
    loadData()
  }).catch(() => {})
}

const handleExport = () => {
  axios({
    url: 'http://localhost:9090/api/question/export',
    method: 'GET',
    responseType: 'blob'
  }).then(res => {
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.style.display = 'none'
    a.href = url
    a.download = '题库导出.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  }).catch(() => {
    ElMessage.error('导出失败，请稍后重试')
  })
}

// 刷新统计：先计算 P 值，再计算 D 值
const handleRecalcStats = () => {
  request.post('/api/question/recalcP')
    .then(() => {
      return request.post('/api/question/recalcD')
    })
    .then(() => {
      ElMessage.success('统计刷新完成')
      loadData()
    })
    .catch(() => {
      // 错误提示由拦截器统一处理
    })
}

onMounted(() => {
  loadKnowledgeList()  // 先加载知识点
  loadData()
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