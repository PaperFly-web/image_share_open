<template>
  <n-config-provider :theme="darkTheme" :locale="locale" :date-locale="dateLocale">

    <n-layout position="absolute">
      <n-layout-header  bordered>
        <n-layout-content  style="--color:rgb(24, 24, 28)">
          <div style="display: flex">
            <logo></logo>
            <n-date-picker @update:value="timeUpdate" class="headerContent" size="medium" v-model:value="range"
                           type="datetimerange" clearable/>
          </div>

        </n-layout-content>

      </n-layout-header>
      <n-layout has-sider position="absolute" style="top: 64px; bottom: 64px;">
        <n-layout-sider bordered
                        show-trigger
                        :inverted="inverted"
                        >
          <Sidebar></Sidebar>
        </n-layout-sider>
        <n-layout content-style="padding: 24px;">
          <router-view/>
        </n-layout>
      </n-layout>
      <n-layout-footer bordered position="absolute" style="height: 64px; padding: 24px; text-align: center;">
        copyright 23333333333
      </n-layout-footer>
    </n-layout>
  </n-config-provider>
</template>

<script>
import Sidebar from './SideBar'
import {onMounted, defineComponent, ref, reactive, inject, onBeforeUnmount} from "vue";
import {useMessage} from 'naive-ui'
import {useStore} from 'vuex'
import pubsub from 'pubsub-js'
import PubSub from "pubsub-js";
import {darkTheme, zhCN, dateZhCN} from 'naive-ui'
import Logo from "../../components/common/Logo";
import router from "../../router";
import {useRoute} from "vue-router";

export default defineComponent({
      components: {
        Logo,
        Sidebar
      },
      setup() {
        const message = useMessage()
        let store = useStore()
        let route = useRoute()
        let range = ref([store.state.startTime, store.state.endTime])

        const timeUpdate = function (value) {
          store.dispatch("updateStartTime", value[0])
          store.dispatch("updateEndTime", value[1])
          //发布消息，通知说store时间变化了
          pubsub.publish("updateStoreTime", null)
        }

        //默认跳转到用户管理页
        if(route.fullPath === "/admin"){
          router.push("/admin/userManage")
        }
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
        })

        onBeforeUnmount(() => {
          PubSub.unsubscribe("successMsg")
          PubSub.unsubscribe("warningMsg")
          PubSub.unsubscribe("errorMsg")
        })

        return {
          range,
          timeUpdate, darkTheme,
          locale:zhCN,
          dateLocale:dateZhCN,
          inverted:ref(false)
        }

      }

    }
)
</script>
<style scoped>

.headerContent {
  padding: 0rem 0.2rem 0.2rem;
  width: 23rem;
}
.n-date-picker{
  align-self: flex-end;
  max-width: 400px;
}
.logo{
  width: 272px;
  text-align: center;
}

</style>