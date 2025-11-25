import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:9090', // 指向你的 Spring Boot 后端端口
    timeout: 5000
})

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== '200') {
            ElMessage.error(res.msg || '系统异常')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default request