import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@/assets/css/global.css'
import '@/assets/css/themes.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const savedTheme = localStorage.getItem('theme')
if (savedTheme) {
  document.documentElement.setAttribute('data-theme', savedTheme)
}

const app = createApp(App)

app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
