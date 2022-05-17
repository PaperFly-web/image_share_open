import {createRouter, createWebHashHistory} from 'vue-router'
import PubSub from "pubsub-js"
import routes from './routes'
import {permit, authority,ROLE_ADMIN,ROLE_SUPER_ADMIN} from './routes'
import {UserInfoUtil} from "../utils/UserInfoUtil";
import {useMyMessage} from "../utils/myMessage";
import {GlobalLoading} from "../utils/globalLoading";

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

const message = useMyMessage


router.beforeEach((to, from, next) => {
    document.title = to.meta.title
    GlobalLoading.startLoading()
    // console.log("path",to.path)
    if(to.path === "/"){
        next("/index")
    }
    //判断路由是否存在
    // console.log(to.meta.type)
    if (to.meta.type === undefined) {
        next("/404")
    }else if(to.meta.type === authority){//访问需要登录的资源
        if(!UserInfoUtil.isLoginAndRemoveLoginOnTokenInvalid()){
            next("/login")
            return
        }
    }else if(to.meta.type === ROLE_ADMIN){
        //先判断是否登录了
        if(!UserInfoUtil.isLoginAndRemoveLoginOnTokenInvalid()){
            next("/login")
            return
        }
        if(UserInfoUtil.getUserRole() !== ROLE_ADMIN && UserInfoUtil.getUserRole() !== ROLE_SUPER_ADMIN){
            message.warning("您没有权限")
            router.back()
        }
    }else if(to.meta.type === ROLE_SUPER_ADMIN){
        //先判断是否登录了
        if(!UserInfoUtil.isLoginAndRemoveLoginOnTokenInvalid()){
            next("/login")
        }
        if(UserInfoUtil.getUserRole() !== ROLE_SUPER_ADMIN){
            message.warning("您没有权限")
            router.back()
        }
    }
    next()
})
export default router

router.afterEach((to,from,next)=>{
    GlobalLoading.stopLoading()
})