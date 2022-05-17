import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/index'
import naiveUI from './naiveUI'

// 通用字体
import 'vfonts/Lato.css'
// 等宽字体
import 'vfonts/FiraCode.css'




const AppBase = createApp(App);


AppBase.use(naiveUI)
    .use(store)
    .use(router)
    .mount('#app')




