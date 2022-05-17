<!--头像点击下滑的内容组件-->
<template>
  <div class="main">
    <n-modal @maskClick="maskClick" :show="true" title="Dialog">
      <n-card
          closable
          @close="maskClick"
          :segmented="{
          content: true,
          footer: 'soft'
       }"
      >
        <template #header>
          <h3 style="text-align: center">{{ fanOrFocus ? '粉丝' : '关注' }}</h3>
        </template>
        <FocusUser v-for="focusUser in focusUserRes"
                   :user-id="focusUser.userTwo.id"
                   :snake-name="focusUser.userTwo.snakeName"
                   :head-img="focusUser.userTwo.headImage"
                   :username="focusUser.userTwo.username"
                   :focus-type="focusUser.focusType"
                   :fan-or-focus="fanOrFocus"
        ></FocusUser>
        <div style="display: flex;justify-content: center">
          <n-spin size="small" v-if="show.loading"/>
          <h3 v-if="fanOrFocus&&!show.loading&&currShowCount===0">暂无数据</h3>
          <h3 v-if="!fanOrFocus&&!show.loading&&currShowCount===0">暂无数据</h3>
        </div>

      </n-card>
    </n-modal>
  </div>
</template>

<script>
import {defineComponent, onMounted, reactive, ref} from 'vue'
import {NModal, NButton, useMessage} from "naive-ui"
import HeadImg from "../common/headImg";
import SnakeNameShow from "../common/SnakeNameShow";
import FocusUser from "./FocusUser";
import {HTTP} from "../../network/request";

export default defineComponent({
  components: {
    FocusUser,
    SnakeNameShow,
    HeadImg,
    NModal,
    NButton
  },
  emits: ["maskClick"],
  props: {
    userId: {
      type: String
    }, show: {
      type: Boolean
    }, fanOrFocus: {//粉丝列表还是关注列表
      type: Boolean
    },
    count: Number
  },
  setup(props, ctx) {
    const message = useMessage()
    let currShowCount = ref(0)
    let count = reactive({
      fan: 0,
      focus: 0
    })
    let show = reactive({
      loading: true
    })

    function maskClick() {
      ctx.emit("maskClick", false)
    }

    let searchData = {
      current: 0,
      size: 10,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ], pages: 0
    }

    let focusUserRes = reactive([])

    //加载列表
    function loadList() {
      searchData.current++
      //如果是粉丝列表
      if (props.fanOrFocus) {
        load("getUserFansPageById")
      } else {
        load("getUserFocusPageById")
      }
    }

    function load(url) {
      HTTP.myHttp({
        method: "post",
        url: `/focususer/${url}/${props.userId}`,
        data: searchData
      }).then(res => {
        // console.log(`/focususer/${url}/${props.userId}`, res.data)
        show.loading = false
        if (res.data.code === 0) {
          searchData.size = res.data.data.size
          searchData.current = res.data.data.current
          searchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            focusUserRes.push(record)
          }
          currShowCount.value = focusUserRes.length
        } else {
          searchData.current--
          message.warning(res.data.msg)
        }
      }).catch(err => {
        show.loading = false
        searchData.current--
      })
    }

    onMounted(() => {
      loadList()
    })

    return {maskClick, focusUserRes, currShowCount, show, count}
  }
})
</script>
<style scoped>
.n-card {
  max-width: 450px;
}

.content {
  overflow-y: scroll;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.name {
  display: flex;
  flex-direction: column;
  color: #8e8e8e;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.headImgAndSnakeName {
  flex-grow: 2;
  display: flex;
  align-items: center;
}

.headImg {
  margin-right: 0.3rem;
}

.btn {
  flex-grow: 3;
  display: flex;
  justify-content: flex-end;
}
</style>