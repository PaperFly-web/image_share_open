<!--关注用户的帖子-->
<template>
  <div>
    <PostShowStage :onlyShowImage="true" :search-url="searchUrl" :data="data" :method="method"></PostShowStage>
  </div>
</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive} from "vue";
import {HTTP} from "../../../network/request";
import ShowPost from "../../../components/post/ShowPost";
import LoadingMore from "../../../components/common/LoadingMore";
import router from "../../../router";
import {useRoute} from "vue-router";
import {NResult} from 'naive-ui'
import PostShowStage from "../../../components/post/PostShowStage";
export default defineComponent({
  components: {PostShowStage, LoadingMore, ShowPost,NResult},
  setup(props, ctx) {
    const route = useRoute();
    let searchUrl = `/favlist/${route.query.favoriteId}`
    let data = reactive({
      size: 10,
      current: 0,
      realCurrent: 0,
      pages: null,
      orders:[
        {
          "column": "update_time",
          "asc": false
        }
      ]
    })
    let method = 'post'

    return {
      searchUrl,data,method
    }
  }
})
</script>

<style scoped>
.postShowMain {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

}
</style>