<!--关注用户的帖子-->
<template>
  <div class="main">
    <div class="silder">
      <notify-silder-show
          :style="data.notifySearchDataUrl==='/notify/postCommentNotifyDetails'?'background-color: #dcdcdc':''"
          @click="postCommentClick()" :count="data.categoryCount.postCommentCount"
          :key="data.categoryCount.postCommentCount"
          have-read-url="/notify/postCommentHaveRead"
          title="回复我的">
        <AtSharp></AtSharp>
      </notify-silder-show>
      <notify-silder-show
          :style="data.notifySearchDataUrl==='/notify/thumbNotifyDetails'?'background-color: #dcdcdc':''"
          @click="thumbClick()" :count="data.categoryCount.thumbCount"
          :key="data.categoryCount.thumbCount"
          have-read-url="/notify/thumbHaveRead"
          title="收到的赞">
        <HeartOutline></HeartOutline>
      </notify-silder-show>
      <notify-silder-show
          :style="data.notifySearchDataUrl==='/notify/focusNotifyDetails'?'background-color: #dcdcdc':''"
          @click="focusClick()" :count="data.categoryCount.focusCount"
          :key="data.categoryCount.focusCount"
          have-read-url="/notify/focusHaveRead"
          title="关注我的">
        <FlowerOutline></FlowerOutline>
      </notify-silder-show>
      <notify-silder-show
          :style="data.notifySearchDataUrl==='/notify/systemMessageDetails'?'background-color: #dcdcdc':''"
          @click="systemMessageClick()" :count="data.categoryCount.systemMessageCount"
          :key="data.categoryCount.systemMessageCount"
          have-read-url="/notify/systemMessageHaveRead"
          title="系统消息">
        <NotificationsOutline></NotificationsOutline>
      </notify-silder-show>
      <notify-silder-show @click="router.push('/personalMessage')"
                          :count="data.categoryCount.personalMessageCount"
                          :key="data.categoryCount.personalMessageCount"
                          title="私信消息">
        <MailOutline></MailOutline>
      </notify-silder-show>

    </div>
    <div ref="notifyContentRef" class="content">
      <!--      <router-view></router-view>-->
      <notify-content :key="data.notifySearchDataUrl" :search-data-url="data.notifySearchDataUrl"></notify-content>
    </div>
  </div>
</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive, ref} from "vue";
import {useRoute} from "vue-router";
import {HTTP} from "../../../network/request";
import TimeShow from "../../../components/common/TimeShow";
import {TrashOutline, AtSharp, HeartOutline, FlowerOutline, MailOutline,NotificationsOutline} from "@vicons/ionicons5";
import {useMessage} from "naive-ui"
import NotifySilderShow from "../../../components/notify/notifySilderShow";
import router from "../../../router";
import NotifyContent from "./notifyContent";

export default {
  components: {
    NotifyContent,NotificationsOutline,
    NotifySilderShow, TimeShow, TrashOutline, AtSharp, HeartOutline, FlowerOutline, MailOutline
  },
  setup(props, ctx) {
    const message = useMessage()
    let route = useRoute()
    let data = reactive({
      categoryCount: {
        focusCount: 0,
        postCommentCount: 0,
        thumbCount: 0,
        personalMessageCount: 0
      }, notifySearchDataUrl: "/notify/postCommentNotifyDetails"
    })

    let notifyContentRef = ref(null)


    onMounted(() => {
      // console.log("重新渲染了")
      HTTP.GET_AUTH("/notify/categoryCount").then(res => {
        if (res.data.code === 0) {
          // console.log(res.data)
          data.categoryCount.focusCount = res.data.data.focusCount
          data.categoryCount.postCommentCount = res.data.data.postCommentCount
          data.categoryCount.thumbCount = res.data.data.thumbCount
          data.categoryCount.personalMessageCount = res.data.data.personalMessageCount
          data.categoryCount.systemMessageCount = res.data.data.systemMessageCount
        }
      })
      /*if (route.fullPath === "/notify") {
        postCommentClick()
      }*/
    })
    let currClick = computed(() => {
      // console.log(route.fullPath);
      return route.fullPath
    })

    function postCommentClick() {
      // router.push('/notify/postComment')
      data.notifySearchDataUrl = "/notify/postCommentNotifyDetails"
    }

    function thumbClick() {
      // console.log("点赞")
      // router.push("/notify/thumb")
      data.notifySearchDataUrl = "/notify/thumbNotifyDetails"
    }

    function focusClick() {

      data.notifySearchDataUrl = "/notify/focusNotifyDetails"
    }

    function systemMessageClick() {
      data.notifySearchDataUrl = "/notify/systemMessageDetails"
    }

    console.log(notifyContentRef)
    return {
      data, router, postCommentClick, thumbClick, focusClick, currClick, notifyContentRef, systemMessageClick
    }
  }
}
</script>

<style scoped>
.main {
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  margin: 0;
  display: flex;
  height: 85vh;
  max-height: 85vh;
}

.silder {
  flex-grow: 0.8;
  overflow-x: hidden;
  border-right: 1px solid rgba(219, 219, 219, 1);
  max-width: 200px;
}

.content {
  flex-grow: 19;
  padding-top: 0.3rem;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  overflow-y: scroll;
}
</style>