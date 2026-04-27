import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    let user = JSON.parse(localStorage.getItem("xm-user") || '{}')
    config.headers['token'] = user.token || ''
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 当权限验证不通过的时候给出提示
        if (res.code === '401') {
            ElMessage.error(res.msg)
            router.push('/login')
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        // 处理不同类型的错误
        if (error.response) {
            // 服务器返回了错误状态码
            const status = error.response.status
            if (status === 404) {
                ElMessage.error('未找到请求接口')
            } else if (status === 500) {
                ElMessage.error('系统异常，请查看后端控制台报错')
            } else if (status === 401) {
                ElMessage.error('未授权，请重新登录')
                router.push('/login')
            } else {
                ElMessage.error(error.response.data?.msg || '请求失败')
            }
        } else if (error.request) {
            // 请求已发出但没有收到响应
            ElMessage.error('网络错误，请稍后再试')
        } else {
            // 其他错误
            ElMessage.error(error.message || '请求失败')
        }
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

export default request
