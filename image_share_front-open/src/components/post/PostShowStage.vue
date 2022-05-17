<!--post分页查询后的展示舞台-->
<template>

  <div>
    <div class="postShowMain" :style="!onlyShowImage?'justify-content: space-around;':'justify-content: flex-start;'">
      <div v-for="p in post">
        <OnlyShowPostImage :view-type="viewType" @deleteFinished="postDeleteFinished" v-if="onlyShowImage"
                           :post="p"></OnlyShowPostImage>
        <ShowPost v-if="!onlyShowImage" @deleteFinished="postDeleteFinished" :height="postHeight" :width="postWidth"
                  :post="p"></ShowPost>
      </div>
    </div>
    <div>
      <LoadingMore @click="loadPost(postSearchData)" v-if="!isFinallyPage " :loading="loading.isLoading"
                   :msg="loading.msg"></LoadingMore>
      <n-result v-if="isEmpty" status="418" title="还没有数据，来喝杯咖啡">
      </n-result>
    </div>

  </div>
</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive} from "vue";
import {HTTP} from "../../network/request";
import ShowPost from "./ShowPost";
import LoadingMore from "../common/LoadingMore";
import router from "../../router";
import {useRoute} from "vue-router";
import {NResult, NBackTop, useMessage} from 'naive-ui'
import OnlyShowPostImage from "./OnlyShowPostImage";

export default defineComponent({
  components: {OnlyShowPostImage, LoadingMore, ShowPost, NResult, NBackTop},
  props: {
    searchUrl: String,//搜索数据的URL
    data: Object,
    method: {//搜索数据的请求方式
      type: String,
      default: "post"
    },
    postHeight: {//帖子的高度
      type: String,
      default: "30vh"
    }, postWidth: {//帖子的宽度
      type: String,
      default: "20vw"
    }, onlyShowImage: {//是否只展示图片，点击图片展示帖子
      type: Boolean,
      default: false
    }, viewType: {//是哪个舞台浏览，主要用户记录浏览详情来源
      type: Number,
      default: 5
    }
  },
  setup(props, ctx) {
    let loading = reactive({
      isLoading: true,
      msg: "下拉加载更多"
    })
    const message = useMessage()


    let post = reactive([])
    let postSearchData = reactive(props.data)

    let isFinallyPage = computed(() => {
      return (postSearchData.realCurrent === postSearchData.pages) || (postSearchData.pages === null || postSearchData.pages === 0)
    })

    //数据是否为空
    let isEmpty = computed(() => {
      return (postSearchData.pages === null || postSearchData.pages === 0) && !loading.isLoading
    })

    /**
     * 加载数据
     * @param searchData
     */
    function loadPost(searchData) {
      postSearchData.current += 1
      loading.msg = "下拉加载更多(可以尝试点击)"
      loading.isLoading = true
      //获取帖子信息
      HTTP.myHttp({
        url: props.searchUrl,
        method: props.method,
        data: props.data
      }).then(res => {
        loading.isLoading = false
        if (res.data.code === 0) {
          postSearchData.current = res.data.data.current
          postSearchData.realCurrent = res.data.data.current
          postSearchData.size = res.data.data.size
          postSearchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            post.push(record)
          }
        } else {
          message.error(res.data.msg)
          postSearchData.current -= 1
        }
      }).catch(err => {
        loading.isLoading = false
        console.log("<<<<<<<<<<</post/getCurrUserPost>>>>>>>>>>>>>>>", err)
        loading.isShow = false
        postSearchData.current -= 1
      })
    }

    //滚轮滚到底部  加载数据
    function scrollBottom() {
      let scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
      let clientHeight = document.documentElement.clientHeight;
      let scrollHeight = document.documentElement.scrollHeight;
      if (scrollTop + clientHeight >= scrollHeight) {
        if (!isFinallyPage.value) {
          loadPost(postSearchData)
        }
      }
    }


    function postDeleteFinished(p) {
      let postSize = post.length
      for (let i = 0; i < postSize; i++) {
        if (p.id === post[i].id) {
          post.splice(i, 1);
          return
        }
      }
    }

    onMounted(() => {
      loadPost(postSearchData)
      window.addEventListener('scroll', scrollBottom);
    })
    onBeforeUnmount(() => {
      window.removeEventListener('scroll', scrollBottom)//页面离开后销毁监听事件
    })
    return {
      loading,
      post,
      isFinallyPage,
      postDeleteFinished,
      loadPost,
      postSearchData,
      isEmpty
    }
  }
})
</script>

<style scoped>
.postShowMain {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;

}
</style>