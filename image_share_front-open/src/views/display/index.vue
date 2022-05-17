<template>
  <Loading :msg="loading.msg" v-if="loading.isShow"
           style="color: #707070 ;font-family:'Microsoft soft';font-weight: 700"></Loading>
  <nav>

    <div id="nav">
      <div id="logo" @click="iconClick('HomeOutline');router.push('/index')">
        <n-image
            width="65"
            preview-disabled
            src="https://image-share-image.oss-cn-beijing.aliyuncs.com/static/logo/logo.jpg"
        />
      </div>
      <div id="search">
        <n-select style="max-width: 100px" v-model:value="searchTypeValue" :options="searchTypeOpt" placeholder="搜索类型"
                  default-value="1"/>
        <n-input clearable v-model:value="searchValue" @keyup.enter="searchClick" maxlength="200" minlength="1"
                 :show-count="true"
                 placeholder="搜索">
          <template #prefix>
            <n-icon size="20">
              <Search/>
            </n-icon>
          </template>
        </n-input>
      </div>
      <div id="icon">
        <n-icon :size="icon.iconSize" @click="iconClick('HomeOutline');router.push('/index')">
          <HomeOutline v-if="icon.showLineOrSharp.HomeOutline"/>
          <HomeSharp v-if="!icon.showLineOrSharp.HomeOutline"/>
        </n-icon>
        <n-icon :size="icon.iconSize" @click="iconClick('PaperPlaneOutline');router.push('/personalMessage')">
          <PaperPlaneOutline v-if="icon.showLineOrSharp.PaperPlaneOutline"/>
          <PaperPlane v-if="!icon.showLineOrSharp.PaperPlaneOutline"/>
        </n-icon>
        <n-icon :size="icon.iconSize" @click="iconClick('AddCircleOutline');addClick()">
          <AddCircleOutline v-if="icon.showLineOrSharp.AddCircleOutline"/>
          <AddCircleSharp v-if="!icon.showLineOrSharp.AddCircleOutline"/>
        </n-icon>
        <n-icon :size="icon.iconSize" @click="iconClick('CompassOutline');router.push('/explore')">
          <CompassOutline v-if="icon.showLineOrSharp.CompassOutline"/>
          <Compass v-if="!icon.showLineOrSharp.CompassOutline"/>
        </n-icon>

        <n-badge :color="notifyMessage.count>0?'red':'black'" :value="notifyMessage.count" :max="99"
                 @click="iconClick('MailOutline');router.push('/notify')">
          <n-icon :size="icon.iconSize">
            <MailOutline v-if="icon.showLineOrSharp.MailOutline"/>
            <Mail v-if="!icon.showLineOrSharp.MailOutline"/>
          </n-icon>
        </n-badge>
        <n-popover trigger="hover" :show-arrow="false">
          <template #trigger>
            <n-avatar
                round
                :size="48"
                :src="userInfo.data.headImage"
                fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
                @click="userHeadClick"
            />
          </template>
          <div v-if="UserInfoUtil.isLoginAndRemoveLoginOnTokenInvalid()">
            <PopoverContent msg="个人主页" @click="userMainPageClick">
              <HappyOutline/>
            </PopoverContent>
            <PopoverContent msg="设置" @click="settingClick">
              <SettingsOutline/>
            </PopoverContent>
            <hr>
            <PopoverContent msg="退出登录" @click="logout">
              <LogOutOutline/>
            </PopoverContent>

          </div>
        </n-popover>

      </div>
    </div>
  </nav>
  <section>
    <n-back-top :bottom="220"/>
    <div id="section">
      <router-view :key="router.currentRoute.value.fullPath">
      </router-view>
    </div>
  </section>

</template>

<script>
import {NAvatar, NIcon, NBadge, NPopover, NDivider, NBackTop, useMessage, useLoadingBar} from 'naive-ui'
import {
  HomeOutline, Search, AddCircleOutline, MailOutline, HeartOutline, PaperPlaneOutline, CompassOutline,
  HomeSharp, AddCircleSharp, Mail, HeartSharp, PaperPlane, Compass, LogOutOutline, HappyOutline, SettingsOutline
} from "@vicons/ionicons5";

import {useRoute} from "vue-router";
import {reactive, onMounted, onBeforeUnmount, ref} from "vue";
import router from "../../router";
import {HTTP, myHttp} from "../../network/request";
import {UserInfoUtil} from "../../utils/UserInfoUtil";
import PopoverContent from "../../components/PopoverContent";
import Loading from "../../components/Loading";
import PubSub from 'pubsub-js'
import Socket from "../../network/socket";

export default {
  components: {
    PopoverContent, Loading, NBackTop,
    NAvatar, NIcon, NBadge, NPopover, NDivider,
    HomeOutline, Search, AddCircleOutline, MailOutline, HeartOutline, PaperPlaneOutline, CompassOutline,
    HomeSharp, AddCircleSharp, Mail, HeartSharp, PaperPlane, Compass, LogOutOutline, HappyOutline, SettingsOutline
  },
  setup() {
    const message = useMessage()
    const route = useRoute()
    let userInfo = reactive(UserInfoUtil.getUserInfo())
    let icon = reactive({
      iconSize: 30,
      showLineOrSharp: {
        "HomeOutline": true,
        "PaperPlaneOutline": true,
        "AddCircleOutline": true,
        "CompassOutline": true,
        "MailOutline": true,
        "HeartOutline": true
      }
    })
    let searchValue = ref("")

    //icon的各个点击事件处理
    function iconClick(iconName) {
      iconChangeColor(iconName)
    }

    function iconChangeColor(iconName) {
      for (let showLineOrSharpKey in icon.showLineOrSharp) {
        if (showLineOrSharpKey === iconName) {
          icon.showLineOrSharp[showLineOrSharpKey] = false;
        } else {
          icon.showLineOrSharp[showLineOrSharpKey] = true;
        }
      }
    }


    //退出登录
    function logout() {
      loading.msg = "退出登录中"
      loading.isShow = true
      myHttp({
        url: '/logout',
        method: "post"
      }).then(res => {
        loading.isShow = false
        if (res.data.code !== 0) {
          message.error(res.data.msg)
        } else {
          //移除登录状态
          UserInfoUtil.removeLogin()
          message.success(res.data.msg)
          // location.reload()
        }
      }).catch(err => {
        loading.isShow = false
        console.log(err);
        message.error("网络繁忙，请稍后再试")
      })
      router.push('/login')
    }

    //个人主页
    function userMainPageClick() {
      router.push('/userMainPage')
    }

    function settingClick() {
      router.push('/userMainPage/main/change')
    }

    function userHeadClick() {
      if (!UserInfoUtil.isLoginAndRemoveLoginOnTokenInvalid()) {
        router.push("/login")
      }
    }

    function addClick() {
      router.push("/post/upload")
    }


    let loading = reactive({
      isShow: false,
      msg: ""
    })


    //消息通知
    let notifyMessage = reactive({
      userId: UserInfoUtil.getUserId(),
      count: 0,
      desc: ""
    })
    // 1、检查有多少新消息
    let socket = Socket.getSocket(`/ws/${UserInfoUtil.getUserToken()}/notify`)

    let loadingBar = useLoadingBar()


    function searchClick() {
      if (!searchValue.value) {
        message.warning("搜索数据不能为空")
        return
      } else {
        let serTp = searchTypeValue.value
        if(serTp){
          serTp = parseInt(serTp);
        }
        console.log("搜索类型", serTp)
        if (serTp == null || serTp === 1) {
          window.open(`/#/search?keyword=${searchValue.value}`)
        } else if (serTp === 2) {
          window.open(`/#/search?topic=${searchValue.value}`)
        } else if(serTp === 3) {
          window.open(`/#/search?place=${searchValue.value}`)
        }else {
          window.open(`/#/search?userName=${searchValue.value}`)
        }

      }
    }

    let searchTypeOpt = [
      {
        label: "图片",
        value: '1'
      },
      {
        label: "话题",
        value: '2'
      },
      {
        label: "地点",
        value: '3'
      },
      {
        label: "用户",
        value: '4'
      }
    ]
    let searchTypeValue = ref(null);

    onMounted(() => {
      //做消息提示的扩展
      PubSub.subscribe("successMsg", (msgName, data) => {
        message.success(data)
      })
      PubSub.subscribe("warningMsg", (msgName, data) => {
        message.warning(data)
      })
      PubSub.subscribe("errorMsg", (msgName, data) => {
        message.error(data)
      })
      // console.log("首页组件加载")
      PubSub.subscribe("userInfoChange", (msgName, data) => {
        // console.log("首页监听到用户信息发生改变")
        userInfo.data.headImage = data.data.headImage
      })
      PubSub.subscribe("globalLoadingIsShow", (msgName, data) => {
        // console.log("监听到全局加载发生变化",data)
        if (data) {
          loadingBar.start()
        } else {
          loadingBar.finish()
        }
      })


      socket.onopen = function (e) {
        console.log("消息通知：建立连接", e.id, e)
      }

      socket.onmessage = function (res) {
        let data = JSON.parse(res.data)
        if (data.code !== 0) {
          message.error(data.msg)
        } else {
          notifyMessage.count = data.data.count
          notifyMessage.desc = data.data.desc
        }
        console.log("消息通知：接收websocket返回的数据", data)
      }

      socket.onclose = function (e) {
        console.log("消息通知：服务断开连接", e.reason, e.code, e.wasClean)
      }

      socket.onerror = function (e) {
        console.log('消息通知：通讯发生错误', e)
      }

      window.onclose = function () {
        socket.close()
      }
    })


    onBeforeUnmount(() => {
      PubSub.unsubscribe("userInfoChange")
      // PubSub.unsubscribe("netWorkError")
      PubSub.unsubscribe("globalLoadingIsShow")
      PubSub.unsubscribe("successMsg")
      PubSub.unsubscribe("warningMsg")
      PubSub.unsubscribe("errorMsg")
      socket.close()
    })

    return {
      router,
      UserInfoUtil,
      loading,
      notifyMessage,
      userInfo,
      icon,
      iconClick,
      logout,
      userHeadClick,
      userMainPageClick,
      addClick,
      settingClick,
      searchValue,
      searchClick,
      searchTypeOpt,
      searchTypeValue
    }
  }
}
</script>
<style scoped>
nav {
  background-color: #ffffff;
  border-bottom: 1px solid rgba(219, 219, 219, 1);
  height: 65px;
  position: fixed;
  width: 100%;
  z-index: 1;
}

#nav {
  display: -webkit-box;
  display: -webkit-flex;
  display: -ms-flexbox;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: center;
  transition: height .2s ease-in-out;
  align-items: center;
  max-width: 975px;
  padding: 0 20px;
  margin: 0 auto;
}

section {
  position: relative;
  background-color: #fcfcfc;
  top: 66px;
  padding: 0 20px;
  height: 100%;

}

#section {
  max-width: 975px;
  padding-top: 30px;
  margin: 0 auto;
}

#logo {
  flex-grow: 0.2;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

#search {
  flex-grow: 0.3;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

#icon {
  flex-grow: 0.5;
  display: flex;
  justify-content: space-between;
  margin-left: 10px;
  align-items: center;
  height: 60px;
}

.userInfo {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  cursor: pointer;
}

hr {
  border: 0;
  border-top: 1.5px solid #d0d0d5;
}
</style>