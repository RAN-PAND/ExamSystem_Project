<template>
  <el-card>
    <!-- 1. 顶部搜索与操作栏 -->
    <div class="header-box">
      <div class="search">
        <el-input 
          v-model="searchName" 
          placeholder="搜索知识点名称..." 
          style="width: 200px; margin-right: 10px;" 
          clearable 
          @clear="loadData" 
        />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-button type="success" @click="openDialog(null)">+ 新增知识点</el-button>
    </div>

    <!-- 2. 数据表格 -->
    <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="知识点名称" min-width="200" />
      <el-table-column prop="target" label="教学目标" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.target === '了解'" type="info">了解</el-tag>
          <el-tag v-else-if="scope.row.target === '理解'" type="primary">理解</el-tag>
          <el-tag v-else-if="scope.row.target === '掌握'" type="success">掌握</el-tag>
          <el-tag v-else-if="scope.row.target === '应用'" type="warning">应用</el-tag>
          <el-tag v-else>{{ scope.row.target }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chapter" label="所属章节" width="150" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 3. 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑知识点' : '新增知识点'" 
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="知识点名称">
          <el-input v-model="form.name" placeholder="例如：数据库概念" />
        </el-form-item>
        <el-form-item label="教学目标">
          <el-select v-model="form.target" placeholder="请选择教学目标" style="width: 100%;">
            <el-option label="了解" value="了解" />
            <el-option label="理解" value="理解" />
            <el-option label="掌握" value="掌握" />
            <el-option label="应用" value="应用" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属章节">
          <el-input v-model="form.chapter" placeholder="例如：第一章" />
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
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)

const form = reactive({
  id: null,
  name: '',
  target: '掌握',
  chapter: ''
})

// 加载数据
const loadData = () => {
  request.get('/api/knowledge/list', {
    params: { name: searchName.value }
  }).then(res => {
    tableData.value = res.data
  })
}

// 打开弹窗
const openDialog = (row) => {
  if (row) {
    // 编辑模式：复制数据
    Object.assign(form, row)
  } else {
    // 新增模式：清空数据
    form.id = null
    form.name = ''
    form.target = '掌握'
    form.chapter = ''
  }
  dialogVisible.value = true
}

// 保存（新增或修改）
const save = () => {
  if (!form.name || !form.target || !form.chapter) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (form.id) {
    // 修改
    request.put('/api/knowledge/update', form).then(() => {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      loadData()
    })
  } else {
    // 新增
    request.post('/api/knowledge/add', form).then(() => {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      loadData()
    })
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个知识点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/api/knowledge/delete/${id}`).then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
  })
}

onMounted(() => {
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
</style>