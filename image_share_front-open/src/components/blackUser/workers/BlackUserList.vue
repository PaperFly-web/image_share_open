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
          <h3 style="text-align: center">黑名单</h3>
        </template>
        <BlackUser v-for="focusUser in blackUserRes"
                   :user-id="focusUser.id"
                   :snake-name="focusUser.snakeName"
                   :head-img="focusUser.headImage"
                   :username="focusUser.username"
        ></BlackUser>
        <div style="display: flex;justify-content: center">
          <loading-more :loading="show.loading" v-if="!isFinallyPage" @click="loadMore"></loading-more>
          <h3 v-if="!show.loading&&currShowCount===0">暂时还没有数据</h3>
        </div>

      </n-card>
    </n-modal>
  </div>
</template>

<script>
import {computed, defineComponent, onMounted, reactive, ref} from 'vue'
import {NModal, NButton, useMessage} from "naive-ui"
import HeadImg from "../../common/headImg";
import SnakeNameShow from "../../common/SnakeNameShow";
import {HTTP} from "../../../network/request";
import BlackUser from "./BlackUser";
import LoadingMore from "../../common/LoadingMore";

export default defineComponent({
  components: {
    LoadingMore,
    BlackUser,
    SnakeNameShow,
    HeadImg,
    NModal,
    NButton
  },
  emits: ["maskClick"],
  props: {
    show: {
      type: Boolean
    }
  },
  setup(props, ctx) {
    const message = useMessage()
    let currShowCount = ref(0)
    let show = reactive({
      loading: true
    })

    function maskClick() {
      ctx.emit("maskClick", false)
    }

    let searchData = {
      current: 0,
      size: 10,
      realCurrent:0,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ], pages: null
    }

    let blackUserRes = reactive([])

    //加载列表
    function loadMore() {
      searchData.current++
      HTTP.POST_AUTH(`/blackuser/getCurrUserBlackUser`, searchData).then(res => {
        show.loading = false
        if (res.data.code === 0) {
          searchData.size = res.data.data.size
          searchData.current = res.data.data.current
          searchData.realCurrent = res.data.data.current
          searchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            blackUserRes.push(record)
          }
          currShowCount.value = blackUserRes.length
        } else {
          searchData.current--
          message.warning(res.data.msg)
        }
      }).catch(err => {
        show.loading = false
        searchData.current--
      })
    }

    //是否为最后一页
    let isFinallyPage = computed(() => {
      return (searchData.realCurrent === searchData.pages) || (searchData.pages === null || searchData.pages === 0)
    })

    onMounted(() => {
      loadMore()
    })

    return {maskClick, loadMore, blackUserRes, currShowCount, show, isFinallyPage,searchData}
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